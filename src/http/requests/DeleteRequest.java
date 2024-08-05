package http.requests;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class DeleteRequest
{
  String url;
  ArrayList<BasicNameValuePair> nameValuePairs;
  HashMap<String, File> nameFilePairs;
  ArrayList<BasicNameValuePair> headerPairs;

  String content;
  String encoding;
  HttpResponse response;
  UsernamePasswordCredentials creds;

  public DeleteRequest(String url)
  {
    this(url, "ISO-8859-1");
  }

  public DeleteRequest(String url, String encoding) 
  {
    this.url = url;
    this.encoding = encoding;
    nameValuePairs = new ArrayList<BasicNameValuePair>();
    nameFilePairs = new HashMap<String, File>();
    this.headerPairs = new ArrayList<BasicNameValuePair>();
  }

  public void addUser(String user, String pwd) 
  {
    creds = new UsernamePasswordCredentials(user, pwd);
  }

  public void addHeaderPair(String key, String value) {
    BasicNameValuePair nvp = new BasicNameValuePair(key, value);
    headerPairs.add(nvp);
  } 

  public void addData(String key, String value) 
  {
    BasicNameValuePair nvp = new BasicNameValuePair(key, value);
    nameValuePairs.add(nvp);
  }
  
  public void addFile(String name, File f) {
    nameFilePairs.put(name, f);
  }

  public void addFile(String name, String path) {
    File f = new File(path);
    nameFilePairs.put(name, f);
  }

  public void send() 
  {
    try {
      DefaultHttpClient httpClient = new DefaultHttpClient();
      HttpDelete httpDelete = new HttpDelete(url);

      if (creds != null) {
        httpDelete.addHeader(new BasicScheme().authenticate(creds, httpDelete, null));
      }

      if (nameFilePairs.isEmpty()) {
        //httpDelete.setEntity(new UrlEncodedFormEntity(nameValuePairs, encoding));
      } else {
        MultipartEntity mentity = new MultipartEntity();  
        Iterator<Entry<String, File>> it = nameFilePairs.entrySet().iterator();
        while (it.hasNext()) {
          Entry<String, File> pair =  it.next();
          String name = (String) pair.getKey();
          File f = (File) pair.getValue();
          mentity.addPart(name, new FileBody(f));
        }        
        for (NameValuePair nvp : nameValuePairs) {
          mentity.addPart(nvp.getName(), new StringBody(nvp.getValue()));
        }
      }

      Iterator<BasicNameValuePair> headerIterator = headerPairs.iterator();
      while (headerIterator.hasNext()) {
        BasicNameValuePair headerPair = headerIterator.next();
        httpDelete.addHeader(headerPair.getName(), headerPair.getValue());
      }

      response = httpClient.execute( httpDelete );
      HttpEntity   entity   = response.getEntity();
      this.content = EntityUtils.toString(response.getEntity());

      if ( entity != null ) EntityUtils.consume(entity);

      httpClient.getConnectionManager().shutdown();

      // Clear it out for the next time
      nameValuePairs.clear();
      nameFilePairs.clear();
      headerPairs.clear();
    } 
    catch( Exception e ) { 
      e.printStackTrace();
    }
  }
  /* Getters
   _____________________________________________________________ */

  public String getContent()
  {
    return this.content;
  }

  public String getHeader(String name)
  {
    Header header = response.getFirstHeader(name);
    if (header == null)
    {
      return "";
    } else
    {
      return header.getValue();
    }
  }

  public void sendPostRequest(String url, String jsonPayload) throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
        HttpPost httpPost = new HttpPost(url);
        StringEntity entity = new StringEntity(jsonPayload, ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8));
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");

        // Add headers if any
        for (BasicNameValuePair headerPair : headerPairs) {
            httpPost.addHeader(headerPair.getName(), headerPair.getValue());
        }

        response = httpClient.execute(httpPost);
        this.content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
    }
  }

  public void addHeader(String name, String value) {
    this.headerPairs.add(new BasicNameValuePair(name, value));
  }
}