package com.archesky.auth.library.service;

import com.archesky.auth.library.model.Token;
import com.google.gson.Gson;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;

@Service
public class TokenService {

    private Request get(final String url, final Map<String, String> params, Headers headers) {
        final HttpUrl.Builder httpBuilder = requireNonNull(
                HttpUrl.parse(url), "Could not parse url - " + url
        ).newBuilder();
        if (params != null) {
            for(final Map.Entry<String, String> param : params.entrySet()) {
                httpBuilder.addQueryParameter(param.getKey(),param.getValue());
            }
        }
        return new Request.Builder().url(httpBuilder.build()).headers(headers).build();
    }

    public Token validateToken(final String serverUrl, final String token, final String hostName) {
        final OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(10, SECONDS)
                .readTimeout(30, SECONDS)
                .build();

        final Request request = get(
                serverUrl,
                Map.of("token", token, "hostname", hostName),
                Headers.of("hostname", hostName)
        );

        try (final Response response = client.newCall(request).execute()) {
            return new Gson().fromJson(requireNonNull(
                    response.body(), "Response body is null"
            ).string(), Token.class);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
