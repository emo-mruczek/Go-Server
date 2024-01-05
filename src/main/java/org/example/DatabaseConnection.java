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

  private static void dothings() {
    String uname = "root";
    String pass = "qwerty";
    String query = "SELECT * FROM actor";

    try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", uname, pass);
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

  public  static void retrieve() {
    try {
      TrustManager[] trustAllCerts = new TrustManager[]{
          new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
              return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
              // Perform certificate validation if needed
            }
          }
      };

      SSLContext sslContext = null;
      try {
        sslContext = SSLContext.getInstance("TLS");
      } catch (NoSuchAlgorithmException ex) {
        throw new RuntimeException(ex);
      }
      try {
        sslContext.init(null, trustAllCerts, null);
      } catch (KeyManagementException ex) {
        throw new RuntimeException(ex);
      }

      dothings();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
