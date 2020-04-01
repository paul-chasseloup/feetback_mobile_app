package paulchasseloup.feetback_mobile_app;

import com.apollographql.apollo.ApolloClient;

import okhttp3.OkHttpClient;

public class ApolloConnector {

    private static final String BASE_URL = "https://ppe-feetback.herokuapp.com/graphql";
//    private static final String BASE_URL = "http://10.7.17.207:4000/graphql";

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
