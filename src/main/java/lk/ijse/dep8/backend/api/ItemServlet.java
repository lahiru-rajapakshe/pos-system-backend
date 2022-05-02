package lk.ijse.dep8.backend.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import lk.ijse.dep8.backend.DTO.ItemDTO;
//import lk.ijse.dep8.backend.dto.BookDTO2;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialBlob;
import javax.xml.bind.ValidationException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@MultipartConfig(location = "/tmp", maxFileSize = 15 * 1024 * 1024)
@WebServlet(name = "BookServlet2", value = "/item/*")
public class ItemServlet extends HttpServlet {

    @Resource(name = "java:comp/env/jdbc/pool4pos")
    private volatile DataSource pool;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doSaveOrUpdate(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doSaveOrUpdate(req, resp);
    }

    private void doSaveOrUpdate(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (req.getContentType() == null ||
                !req.getContentType().toLowerCase().startsWith("multipart/form-data")) {
            res.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }

        String method = req.getMethod();
        String pathInfo = req.getPathInfo();

        System.out.println(req.getRequestURI());
        if (method.equals("POST") && (pathInfo != null && !pathInfo.equals("/"))) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else if (method.equals("PUT") && !(pathInfo != null &&
                pathInfo.substring(1).matches("\\d+[/]?"))) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Item does not exist");
            return;
        }

        try {
            String isbn = req.getParameter("isbn");
            String name = req.getParameter("name");
            String author = req.getParameter("author");
            Part preview = req.getPart("preview");
            String price = req.getParameter("price");


            ItemDTO book;
            if (preview != null && !preview.getSubmittedFileName().isEmpty()) {

                if (!preview.getContentType().toLowerCase().startsWith("image/")) {
                    throw new ValidationException("Invalid preview");
                }

                byte[] buffer = new byte[(int) preview.getSize()];
                preview.getInputStream().read(buffer);
                book = new ItemDTO(isbn, name, author,price, buffer);
            } else {
                book = new ItemDTO(isbn, name, author);
            }

            if (method.equals("POST") &&
                    (book.getIsbn() == null || !book.getIsbn().matches("\\d+"))) {
                throw new ValidationException("Invalid ISBN");
            } else if (book.getName() == null || !book.getName().matches(".+")) {
                throw new ValidationException("Invalid Book Name");
            } else if (book.getAuthor() == null || !book.getAuthor().matches("[A-Za-z0-9 ]+")) {
                throw new ValidationException("Invalid Author Name");
            }

            if (method.equals("PUT")) {
                book.setIsbn(pathInfo.replaceAll("[/]", ""));
            }

            try (Connection connection = pool.getConnection()) {
                PreparedStatement stm = connection.
                        prepareStatement("SELECT * FROM item WHERE isbn=?");
                stm.setString(1, book.getIsbn());
                ResultSet rst = stm.executeQuery();

                if (rst.next()) {
                    if (method.equals("POST")) {
                        res.sendError(HttpServletResponse.SC_CONFLICT, "Item already exists");
                    } else {
                        stm = connection.
                                prepareStatement("UPDATE item SET name=?, company=?, preview=? WHERE isbn=?");
                        stm.setString(1, book.getName());
                        stm.setString(2, book.getAuthor());
                        stm.setBlob(3, book.getPreview() != null ? new SerialBlob(book.getPreview()) : null);
                        stm.setString(4, book.getIsbn());
                        if (stm.executeUpdate() != 1) {
                            throw new RuntimeException("Failed to update the Item details");
                        }
                        res.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    }
                } else {
                    stm = connection.prepareStatement("INSERT INTO item (isbn, name, company, preview,price) VALUES (?,?,?,?,?)");
                    stm.setString(1, book.getIsbn());
                    stm.setString(2, book.getName());
                    stm.setString(3, book.getAuthor());
                    stm.setBlob(4, book.getPreview() == null ? null : new SerialBlob(book.getPreview()));
                    stm.setString(5,book.getPrice());
                    if (stm.executeUpdate() != 1) {
                        throw new RuntimeException("Failed to add a Item");
                    }
                    res.setStatus(HttpServletResponse.SC_CREATED);
                }
            }
        } catch (ValidationException e) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getPathInfo() == null || req.getPathInfo().equals("/")) {
            resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "Unable to delete all items yet");
            return;
        } else if (req.getPathInfo() != null &&
                !req.getPathInfo().substring(1).matches("\\d+[/]?")) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Item does not exist");
            return;
        }

        String isbn = req.getPathInfo().replaceAll("[/]", "");

        try (Connection connection = pool.getConnection()) {
            PreparedStatement stm = connection.
                    prepareStatement("SELECT * FROM item WHERE isbn=?");
            stm.setString(1, isbn);
            ResultSet rst = stm.executeQuery();

            if (rst.next()) {

                stm = connection.prepareStatement("SELECT * FROM item INNER JOIN issue i on item.isbn = i.isbn WHERE i.isbn=?");
                stm.setString(1, isbn);
                rst = stm.executeQuery();
                if (rst.next()){
                    resp.sendError(HttpServletResponse.SC_GONE, "Item has been issued already");
                    return;
                }

                stm = connection.prepareStatement("DELETE FROM item WHERE isbn=?");
                stm.setString(1, isbn);
                if (stm.executeUpdate() != 1) {
                    throw new RuntimeException("Failed to delete the Item");
                }
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Item does not exist");
            }
        } catch (SQLException | RuntimeException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    System.out.println("get method performmed !");

        if (req.getPathInfo() != null) {
            if (req.getPathInfo().substring(1).matches("\\d+[/]?")){
                searchBook(req, resp);
                return;
            }else if(!req.getPathInfo().equals("/")){
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }

        String query = req.getParameter("q");
        query = "%" + ((query == null) ? "" : query) + "%";

        try (Connection connection = pool.getConnection()) {

            boolean pagination = req.getParameter("page") != null &&
                    req.getParameter("size") != null;
            String sql = "SELECT b.*, i.id FROM item b LEFT OUTER JOIN odr i on b.isbn = i.isbn WHERE b.isbn LIKE ? OR b.name LIKE ? OR b.company LIKE ? " + ((pagination) ? "LIMIT ? OFFSET ?" : "");

            PreparedStatement stm = connection.prepareStatement(sql);
            PreparedStatement stmCount = connection.prepareStatement("SELECT * FROM item WHERE isbn LIKE ? OR name LIKE ? OR company LIKE ?");

            stm.setString(1, query);
            stm.setString(2, query);
            stm.setString(3, query);
            stmCount.setString(1, query);
            stmCount.setString(2, query);
            stmCount.setString(3, query);

            if (pagination) {
                int page = Integer.parseInt(req.getParameter("page"));
                int size = Integer.parseInt(req.getParameter("size"));
                stm.setInt(4, size);
                stm.setInt(5, (page - 1) * size);
            }
            ResultSet rst = stm.executeQuery();

            List<ItemDTO> books = new ArrayList<>();

            while (rst.next()) {
                books.add((new ItemDTO(
                        rst.getString("isbn"),
                        rst.getString("name"),
                        rst.getString("company"),
                        rst.getBytes("preview"),
                        rst.getString("id") == null
                )));
            }

            ResultSet rst2 = stmCount.executeQuery();
            if (rst2.next()) {
                resp.setHeader("X-Count", rst2.getString(1));
            }

            resp.setContentType("application/json");
            Jsonb jsonb = JsonbBuilder.create();
            jsonb.toJson(books, resp.getWriter());

        } catch (SQLException t) {
            t.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void searchBook

            (HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        try (Connection connection = pool.getConnection()) {
            PreparedStatement stm = connection.prepareStatement("SELECT b.*, i.id FROM item b LEFT OUTER JOIN odr i on b.isbn = i.isbn WHERE b.isbn = ?");
            stm.setString(1, req.getPathInfo().replaceAll("[/]", ""));
            ResultSet rst = stm.executeQuery();
            if (!rst.next()){
                res.sendError(HttpServletResponse.SC_NOT_FOUND, "Item not found");
                return;
            }

            ItemDTO book = new ItemDTO(rst.getString("isbn"),
                    rst.getString("name"),
                    rst.getString("author"),
                    rst.getBytes("preview"),
                    rst.getString("id") == null);

            res.setContentType("application/json");
            Jsonb jsonb = JsonbBuilder.create();
            jsonb.toJson(book, res.getWriter());
        }catch (SQLException | RuntimeException e){
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}