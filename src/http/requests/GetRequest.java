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

public class GetRequest
{
	String url;
	String content;
	HttpResponse response;
	UsernamePasswordCredentials creds;
	ArrayList<BasicNameValuePair> headerPairs;

	public GetRequest(String url) 
	{
		this.url = url;
		this.headerPairs = new ArrayList<>();
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

	public String getContent() {
		return this.content;
	}

	public String getHeader(String name) {
		Header header = response.getFirstHeader(name);
		if (header == null) {
			return "";
		} else {
			return header.getValue();
		}
	}
}
