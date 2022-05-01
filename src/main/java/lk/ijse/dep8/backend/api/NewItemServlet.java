package lk.ijse.dep8.backend.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
//import lk.ijse.dep8.backend.DTO.BookDTO2;
import lk.ijse.dep8.backend.DTO.NewItemDTO;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import javax.sql.DataSource;
import javax.xml.bind.ValidationException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "NewItemServlet", value = {"/item/*"})

public class NewItemServlet extends HttpServlet {

    @Resource(name = "java:comp/env/jdbc/pool4pos")
    private volatile DataSource pool;
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Get method is perfoming");
        doSaveOrUpdate(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doSaveOrUpdate(request, response);
    }

    private void doSaveOrUpdate(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (req.getContentType() == null ||
                !req.getContentType().toLowerCase().startsWith("application/json")) {
            res.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }

        String method = req.getMethod();
        String pathInfo = req.getPathInfo();

        if (method.equals("POST") && (pathInfo != null && !pathInfo.equals("/"))) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else if (method.equals("PUT") && !(pathInfo != null &&
                pathInfo.substring(1).matches("\\d{9}[Vv][/]?"))) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Member does not exist");
            return;
        }

        try {
            Jsonb jsonb = JsonbBuilder.create();
            NewItemDTO member = jsonb.fromJson(req.getReader(), NewItemDTO.class);
            if (method.equals("POST") &&
                    (member.getNic() == null || !member.getNic().matches("\\d{9}[Vv]"))) {
                throw new ValidationException("Invalid NIC");
            } else if (member.getName() == null || !member.getName().matches("[A-Za-z ]+")) {
                throw new ValidationException("Invalid Name");
            } else if (member.getContact() == null || !member.getContact().matches("\\d{3}-\\d{7}")) {
                throw new ValidationException("Invalid contact number");
            }

            if (method.equals("PUT")) {
                member.setNic(pathInfo.replaceAll("[/]", ""));
            }

            try (Connection connection = pool.getConnection()) {
                PreparedStatement stm = connection.prepareStatement("SELECT * FROM cus WHERE nic=?");
                stm.setString(1, member.getNic());
                ResultSet rst = stm.executeQuery();

                if (rst.next()) {
                    if (method.equals("POST")) {
                        res.sendError(HttpServletResponse.SC_CONFLICT, "Member already exists");
                    } else {
                        stm = connection.prepareStatement("UPDATE cus SET name=?, contact=? WHERE nic=?");
                        stm.setString(1, member.getName());
                        stm.setString(2, member.getContact());
                        stm.setString(3, member.getNic());
                        if (stm.executeUpdate() != 1) {
                            throw new RuntimeException("Failed to update the member");
                        }
                        res.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    }
                } else {
                    stm = connection.prepareStatement("INSERT INTO cus (nic, name, contact) VALUES (?,?,?)");
                    stm.setString(1, member.getNic());
                    stm.setString(2, member.getName());
                    stm.setString(3, member.getContact());
                    if (stm.executeUpdate() != 1) {
                        throw new RuntimeException("Failed to register the member");
                    }
                    res.setStatus(HttpServletResponse.SC_CREATED);
                }
            }

        } catch (JsonbException | ValidationException e) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    (e instanceof JsonbException) ? "Invalid JSON" : e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


}