package org.example;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseConnection {

  public static void retrieve()  {
    String dbUserName = "root";
    String dbPassword = "qwerty";
    String dbName = "sakila";
    String query = "SELECT * FROM actor";

    try (

        Connection con = DriverManager.getConnection("jdbc:mysql://localhost/" + dbName + "?user=" + dbUserName + "&password=" + dbPassword + "&useUnicode=true&characterEncoding=UTF-8");
         PreparedStatement st = con.prepareStatement(query);
         ResultSet rs = st.executeQuery()) {


      while (rs.next()) {
        int actorId = rs.getInt("actor_id");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");

        System.out.println("Actor ID: " + actorId + ", Name: " + firstName + " " + lastName);
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

}
