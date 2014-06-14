package com.ute.promoapp;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;


/**
 * Klasa pozwalaj�ca wysy�a� u�ytkownikowi aplikacji
 * wiadomo�ci SMS. Uruchamia proces wysy�ania w osobnym w�tku. 
 *
 */
public class SendSMS {
	
	/**
	 * Metoda wysy�aj�ca u�ytkownikowi wiadomo�� SMS
	 * za pomoc� OrangeApi. Metoda dzia�a w tle od g��wnego w�tku aplikacji.
	 */
	protected Object doInBackground(String... msg) {
		String from = "789101781";
		String to = "789101781";
	   	String adres = "https://api2.orange.pl/sendsms/?from="+from+"&to="+to+"&msg="+Arrays.toString(msg) ;
	   	try {    	 
	      	 HttpHost targetHost = new HttpHost("api2.orange.pl", 443, "https"); 
	      	 DefaultHttpClient client = new DefaultHttpClient();  
	      	 client.getCredentialsProvider().setCredentials(new AuthScope(targetHost.getHostName(), 
	      			 targetHost.getPort()), new UsernamePasswordCredentials("48789101781", "TSS7C347ENHMLE"));

	      	 sslClient(client);

	      	 HttpGet httpget = new HttpGet(adres); 
	      	 HttpResponse response = client.execute( httpget);
	       	 
	   	}catch (Exception e) {
	   		System.out.println("!!!!!"+e);
	   	}
	   	return null;
	}
	
	/**
	 * Metoda tworz�ca i przypisaj�ca certyfikat
	 * do klienta sesji HTTP.
	 * @param client - klient sesji HTTP
	 */
	private void sslClient(HttpClient client) {
        try {
            X509TrustManager tm = new X509TrustManager() { 
                public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{tm}, null);
            MySSLSocketFactory ssf = new MySSLSocketFactory(ctx);
            ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            
            ClientConnectionManager ccm = client.getConnectionManager();
            SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(new Scheme("https", ssf, 443));
          //  return new DefaultHttpClient(ccm, client.getParams());
        } catch (Exception e) {
        	System.out.println(e);
        }
    }

}
