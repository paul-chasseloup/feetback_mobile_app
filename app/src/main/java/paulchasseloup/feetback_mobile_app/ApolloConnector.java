package paulchasseloup.feetback_mobile_app;

import com.apollographql.apollo.ApolloClient;

import okhttp3.OkHttpClient;

public class ApolloConnector {

    // localhost doesn't work on the emulator
    private static final String BASE_URL = "http://10.7.16.106:4000/graphql";

    public static ApolloClient setupApollo(){

        OkHttpClient okHttpClient = new OkHttpClient
                .Builder()
                .build();

        return ApolloClient.builder()
                .serverUrl(BASE_URL)
                .okHttpClient(okHttpClient)
                .build();
    }

}