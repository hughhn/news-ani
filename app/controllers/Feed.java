package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.typesafe.config.ConfigFactory;
import play.*;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.OAuth1Info;
import securesocial.core.java.SecureSocial;
import securesocial.core.java.SecuredAction;
import services.User;
import twitter4j.*;
import views.html.feed;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeoutException;

import play.libs.ws.*;
import play.libs.F.Promise;

/**
 * Created by hugh_sd on 4/8/15.
 */
public class Feed extends Controller {

    public static String baseUrl = null;
    public static play.Logger.ALogger logger = play.Logger.of("application.controllers.Feed");

    public static class Item {
        public SyndEntry entry;
        public String metadata;

        public Item(SyndEntry entry, String metadata) {
            this.entry = entry;
            this.metadata = metadata;
        }
    }

    public static String getApiBaseUrl() {
        if (baseUrl != null) {
            return baseUrl;
        }

        baseUrl = ConfigFactory.load().getString("analyzer.baseurl");
        logger.debug("baseUrl = " + baseUrl);
        return baseUrl;
    }

    public static Twitter getTwitterInstance() {
        // The factory instance is re-useable and thread safe.
        TwitterFactory factory = new TwitterFactory();
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(Play.application().configuration().getString("securesocial.twitter.consumerKey"),
                Play.application().configuration().getString("securesocial.twitter.consumerSecret"));
        twitter4j.auth.AccessToken accessToken = new twitter4j.auth.AccessToken(token(), tokenSecret());
        twitter.setOAuthAccessToken(accessToken);
        return twitter;
    }

    public static String tokenSecret() {
        String retval = "";
        scala.collection.Iterator iterator = Application.getCurrentUser().main.oAuth1Info().iterator();
        while (iterator.hasNext()) {
            OAuth1Info oAuth1Info = (OAuth1Info) iterator.next();
            retval = oAuth1Info.secret();
        }
        return retval;
    }
    public static String token() {
        String retval = "";
        scala.collection.Iterator iterator = Application.getCurrentUser().main.oAuth1Info().iterator();
        while (iterator.hasNext()) {
            OAuth1Info oAuth1Info = (OAuth1Info) iterator.next();
            retval = oAuth1Info.token();
        }
        return retval;
    }

    public static List<twitter4j.Status> getTweets() {
        Twitter twitter = getTwitterInstance();
        List<twitter4j.Status> tweets = new ArrayList<>();
        try {
            Query query = new Query("popular tweets");
            QueryResult result;
            //do {
                result = twitter.search(query);
                tweets.addAll(result.getTweets());
            //} while ((query = result.nextQuery()) != null);
//            for (twitter4j.Status tweet : tweets) {
//                logger.debug("@" + tweet.getUser().getScreenName() + " - " + tweet.getText());
//            }

            return tweets;
        } catch (TwitterException te) {
            te.printStackTrace();
            logger.error("getTweets() Exception: " + te.getMessage());
            return tweets;
        }
    }

    public static List<SyndEntry> getSyndFeeds() {
        List<SyndEntry> entries = new ArrayList<>();
        try {
            URL feedUrl = new URL("https://news.ycombinator.com/rss");
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new InputStreamReader(feedUrl.openStream()));
            //logger.debug("feed: \n" + feed);

            return feed.getEntries();
        } catch (FeedException | IOException e) {
            e.printStackTrace();
            logger.error("getSyndFeeds() Exception");
            return entries;
        }

    }

    public static Promise<JsonNode> getMetadata(String url) {
        String api = getApiBaseUrl() + "metadata/";

        WSRequestHolder holder = WS
                .url(api)
                //.setTimeout(2000)
                .setQueryParameter("url", url);
        return holder.get().map(response -> response.asJson());
    }

    @SecuredAction
    public static Promise<Result> list() {
        User user = (User) ctx().args.get(SecureSocial.USER_KEY);

        List<SyndEntry> entries = getSyndFeeds();

        List<Promise<JsonNode>> list = new ArrayList<>();
        for (final SyndEntry entry : entries) {
            Promise<JsonNode> metadataJson = getMetadata(entry.getLink());
            metadataJson.recover(throwable -> {
                if (throwable instanceof TimeoutException) {
                    logger.debug("TimeoutException: " + entry.getLink());
                } else if (throwable instanceof ConnectException) {
                    logger.debug("ConnectException: " + entry.getLink());
                }
                ObjectNode result = Json.newObject();
                result.put("url", "no metadata");
                return result;
            });
            list.add(metadataJson);

            // TODO : only analyze 1 article for now
            break;
        }

        Promise<List<JsonNode>> promises = Promise.sequence(list);
        return promises.map(new F.Function<List<JsonNode>, Result>() {
            @Override
            public Result apply(List<JsonNode> jsonNodes) throws Throwable {
                List<String> items = new ArrayList<String>();
                for (JsonNode node : jsonNodes) {
                    items.add(node.get("url").toString());
                }
                return ok(feed.render(user, getTweets(), items));
            }
        }).recover(throwable -> {
            if (throwable instanceof TimeoutException) {
                logger.debug("promises TimeoutException!");
            } else if (throwable instanceof ConnectException) {
                logger.debug("promises ConnectException!");
            }
            List<String> items = new ArrayList<String>();
            return ok(feed.render(user, getTweets(), items));
        });
    }


}
