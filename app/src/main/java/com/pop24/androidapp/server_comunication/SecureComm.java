package com.pop24.androidapp.server_comunication;

import android.os.AsyncTask;
import android.util.Log;

import com.pop24.androidapp.MainActivity;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by Tomas on 9. 5. 2016.
 */
public class SecureComm {
    String tag = "SecureComm";
    String ip = "147.175.145.110";
    MainActivity activity;
    KeyStore keyStore;

    public SecureComm(MainActivity activity){
        this.activity  =activity;
    }

    public void executeQuery() {

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = new BufferedInputStream(activity.getAssets().open("apache.crt"));
            Certificate ca = cf.generateCertificate(caInput);

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);
        } catch (Exception e) {
            e.printStackTrace();
        }


       //String url = "https://" + ip + "/tomasweb/access.php/?action=getUserHistory&id_user=270";
       // HttpsURLConnection urlConnection = httpsPost(url, keyStore);
        new ServerCall().execute();
    }

    private class ServerCall extends AsyncTask<Void, Void, Boolean>{

        @Override
        protected Boolean doInBackground(Void... voids) {
            String urlString = "https://" + ip + "/tomasweb/access.php/?action=getUserHistory&id_user=270";
            Log.d(tag, "startQuery : " + urlString);
            //HttpsURLConnection urlConnection = httpsPost(url, keyStore);

            try {
                // Create a TrustManager that trusts the CAs in our KeyStore
                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                tmf.init(keyStore);

                // Vytvori spojenie ale este sa nepripoji (aj ked tam je openConnection)
                URL url = new URL(urlString);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();  // does not establish the actual connection

                // Create an SSLContext that uses our TrustManager
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, tmf.getTrustManagers(), null);

                //HttpsURLConnection.setDefaultSSLSocketFactory(NoSSLv3Factory);

                // Tu to nastavujeme len pre toto spojenie - to co je zakomentovane vyssie to nastavuje globalne (asi pre vsetky buduce spojenia)
                //   t.j. nastavuje sa triede HttpsURLConnection a nie na premennej urlConnection ako nizsie
                // Toto mohol byt problem - mozno to funguje tak ze to globalne nastavenie to zmeni len pre buduce spojenia
                urlConnection.setSSLSocketFactory(context.getSocketFactory());

                // Ignoruje hostname v certifikate
                urlConnection.setHostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });

                //urlConnection.setRequestMethod("GET");
                //urlConnection.setDoInput(true);

                InputStream in = urlConnection.getInputStream();

                InputStreamReader isw = new InputStreamReader(in);

                int data = isw.read();
                while (data != -1) {
                    char current = (char) data;
                    data = isw.read();
                    System.out.print(current);
                    Log.d(tag, "current : " + current);
                }

                urlConnection.connect();


            /*
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlConnection.setDoInput(true);
            // Send post request Specifies whether this URLConnection allows sending data.
            urlConnection.setDoOutput(true);
            // Send post request
            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
            wr.write(customer.toString().getBytes("UTF-8"));
            wr.flush();
            wr.close();

            // Teraz vytvorime skustocne spojenie cez siet
            // - az na zaver -  ked sme vsetko nastavili zapisali co chceme odoslat
            urlConnection.connect();

            */


                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                //Log.e(TAG, "Failed to establish SSL connection to server: " + ex.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result){
            if(result == true){
                Log.d(tag, "Success executed query");
            }else
                Log.d(tag, "Failed executed query");
        }
    }

    public static HttpsURLConnection httpsPost(String urlString, KeyStore keyStore) {
        try {
            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Vytvori spojenie ale este sa nepripoji (aj ked tam je openConnection)
            URL url = new URL(urlString);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();  // does not establish the actual connection

            // Create an SSLContext that uses our TrustManager
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);

            //HttpsURLConnection.setDefaultSSLSocketFactory(NoSSLv3Factory);

            // Tu to nastavujeme len pre toto spojenie - to co je zakomentovane vyssie to nastavuje globalne (asi pre vsetky buduce spojenia)
            //   t.j. nastavuje sa triede HttpsURLConnection a nie na premennej urlConnection ako nizsie
            // Toto mohol byt problem - mozno to funguje tak ze to globalne nastavenie to zmeni len pre buduce spojenia
            urlConnection.setSSLSocketFactory(context.getSocketFactory());

            // Ignoruje hostname v certifikate
            urlConnection.setHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            //urlConnection.setRequestMethod("GET");
            //urlConnection.setDoInput(true);

            InputStream in = urlConnection.getInputStream();

            InputStreamReader isw = new InputStreamReader(in);

            int data = isw.read();
            while (data != -1) {
                char current = (char) data;
                data = isw.read();
                System.out.print(current);
            }

            urlConnection.connect();


            /*
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlConnection.setDoInput(true);
            // Send post request Specifies whether this URLConnection allows sending data.
            urlConnection.setDoOutput(true);
            // Send post request
            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
            wr.write(customer.toString().getBytes("UTF-8"));
            wr.flush();
            wr.close();

            // Teraz vytvorime skustocne spojenie cez siet
            // - az na zaver -  ked sme vsetko nastavili zapisali co chceme odoslat
            urlConnection.connect();

            */






            return urlConnection;
        } catch (Exception ex) {
            ex.printStackTrace();
            //Log.e(TAG, "Failed to establish SSL connection to server: " + ex.toString());
            return null;
        }


    }

}
