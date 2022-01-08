package com.archesky.auth.library.service;

import com.archesky.auth.library.model.Token;
import com.google.gson.Gson;
import okhttp3.*;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class TokenService {
    private final Logger log = getLogger(this.getClass());

    private Request post(final String url, final Map<String, String> params) {
        log.info("Making auth get request to {} with params {}", url, params);
        final RequestBody requestBody = RequestBody.create(
                new Gson().toJson(params),
                MediaType.get("application/json; charset=utf-8")
        );
        log.info("Created request body: {}", requestBody);
        return new Request.Builder().url(url).post(requestBody).build();
    }

    public Token validateToken(final String serverUrl, final String token, final String hostName) {
        final OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(10, SECONDS)
                .readTimeout(30, SECONDS)
                .build();

        final Request request = post(
                serverUrl,
                Map.of("token", token, "hostname", hostName)
        );

        try (final Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Request failed with code {}", response.code());
                throw new RuntimeException("Unexpected code: " + response.code() + " from server, message: " + response.message());
            }
            final ResponseBody body = requireNonNull(response.body(), "Response body is null");
            final String bodyString = body.string();
            log.info("Received response: {}", bodyString);
            return new Gson().fromJson(bodyString, Token.class);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
