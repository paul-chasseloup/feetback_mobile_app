package paulchasseloup.feetback_mobile_app;

import com.apollographql.apollo.ApolloClient;

import okhttp3.OkHttpClient;

public class ApolloConnector {

    // Server location

    //private static final String BASE_URL = "http://10.7.16.89/PPEusers";    //192.168.43.22
    //private static final String BASE_URL = "https://ppe-feetback.herokuapp.com/graphql";
    private static final String BASE_URL = "https://git.heroku.com/ppe-feetback.git";
//    private static final String BASE_URL = "https://ppe-feetback.herokuapp.com/";
    //private static final String BASE_URL = "https://192.168.43.22:4000/graphql";

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
