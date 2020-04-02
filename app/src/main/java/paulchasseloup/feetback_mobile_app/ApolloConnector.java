package paulchasseloup.feetback_mobile_app;

import com.apollographql.apollo.ApolloClient;

import okhttp3.OkHttpClient;

public class ApolloConnector {

//    private static final String BASE_URL = "http://10.7.16.89/PPEusers";
//    private static final String BASE_URL = "http://192.168.0.14:4000/graphql";
    private static final String BASE_URL = "https://ppe-feetback.herokuapp.com/graphql";

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
