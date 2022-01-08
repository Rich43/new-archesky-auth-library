package com.archesky.auth.library.service;

import com.archesky.auth.library.model.Token;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static okhttp3.MediaType.get;
import static okhttp3.RequestBody.Companion;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class TokenService {
    private final Logger log = getLogger(this.getClass());

    private Request post(final String url, final Map<String, String> params) {
        log.info("Making auth get request to {} with params {}", url, params);
        final RequestBody requestBody = Companion.create(
                new Gson().toJson(params),
                get("application/json; charset=utf-8")
        );
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
            log.debug("Received response: {}", response.body());
            return new Gson().fromJson(requireNonNull(
                    response.body(), "Response body is null"
            ).string(), Token.class);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
