package Servlet;

import DatabaseCredentials.connections;
import static DatabaseCredentials.connections.getConnection;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONValue;

@WebServlet("/Product")
public class Product extends HttpServlet {
Connection conn;
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Content-Type", "text/plain-text");
        try {
            PrintWriter output = response.getWriter();
            String query = "SELECT * FROM product;";
            if (!request.getParameterNames().hasMoreElements()) {
                output.println(resultMethod(query));
            } else {
                String id =request.getParameter("productId");
                output.println(resultMethod("SELECT * FROM product WHERE productId= ?", id));
            }

        } catch (IOException ex) {
            System.err.println("Input output Exception: " + ex.getMessage());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Product.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Product.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Set<String> keyValues = request.getParameterMap().keySet();

        try {
            PrintWriter output = response.getWriter();
            if (keyValues.contains("productId") && keyValues.contains("name") && keyValues.contains("description")
                    && keyValues.contains("quantity")) {
                String productId = request.getParameter("productId");
                String name = request.getParameter("name");
                String description = request.getParameter("description");
                String quantity = request.getParameter("quantity");
                doUpdate("INSERT INTO product (productId,name,description,quantity) VALUES (?, ?, ?, ?)", productId, name, description, quantity);

            } else {
                response.setStatus(500);
                output.println("");
            }

        } catch (IOException ex) {
            System.err.println("Input Output Issue in doPost Method: " + ex.getMessage());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Product.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Product.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) {

        Set<String> keySet = request.getParameterMap().keySet();
        try  {
            PrintWriter out = response.getWriter();
            if (keySet.contains("productId") && keySet.contains("name") && keySet.contains("description") && keySet.contains("quantity")) {
                String productId = request.getParameter("productId");
                String name = request.getParameter("name");
                String description = request.getParameter("description");
                String quantity = request.getParameter("quantity");
                try {
                    doUpdate("update product set productId = ?, name = ?, description = ?, quantity = ? where productId = ?", productId, name, description, quantity, productId);
                } catch (SQLException ex) {
                    Logger.getLogger(Product.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                out.println("Error: Not data found for this input. Please use a URL of the form /products?id=xx&name=XXX&description=XXX&quantity=xx");
            }
        } catch (IOException ex) {
            response.setStatus(500);
            System.out.println("Error in writing output: " + ex.getMessage());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Product.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Set<String> keySet = request.getParameterMap().keySet();
        try {
            PrintWriter out = response.getWriter();
            
            try {
                conn = getConnection();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Product.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (keySet.contains("productId")) {
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM `product` WHERE `productId`=" + request.getParameter("productId"));
                try {
                    pstmt.executeUpdate();
                } catch (SQLException ex) {
                    System.err.println("SQL Exception Error: " + ex.getMessage());
                    out.println("Error in deleting.");
                   
                }
            } else {
                out.println("Error: No data to delete");
                
            }
        } catch (SQLException ex) {
            System.err.println("SQL Exception Error: " + ex.getMessage());
        }
    }

   

    private String resultMethod(String query, String... params) throws ClassNotFoundException, SQLException {
        StringBuilder sb = new StringBuilder();
        String json = "";
        conn = connections.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }
            ResultSet rs = pstmt.executeQuery();
            List l1 = new LinkedList();
            while (rs.next()) {
               
                Map m1 = new LinkedHashMap();
                m1.put("productId", rs.getInt("productId"));
                m1.put("name", rs.getString("name"));
                m1.put("description", rs.getString("description"));
                m1.put("quantity", rs.getInt("quantity"));
                l1.add(m1);

            }

            json = JSONValue.toJSONString(l1);
      
        return json.replace("},", "},\n");
    }

   
    private int doUpdate(String query, String... params) throws ClassNotFoundException, SQLException {
        int num = 0;
         conn = connections.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }
            num = pstmt.executeUpdate();
        
        return num;
    }

}