package controllers;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndLink;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import play.*;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.OAuth1Info;
import securesocial.core.java.SecureSocial;
import securesocial.core.java.SecuredAction;
import services.User;
import twitter4j.*;
import twitter4j.Logger;
import views.html.feed;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by hugh_sd on 4/8/15.
 */
public class Feed extends Controller {
    public static play.Logger.ALogger logger = play.Logger.of("application.controllers.Feed");

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
            entries = feed.getEntries();


            for (SyndEntry entry : entries) {
                logger.debug("@" + entry.getTitle() + " - " + entry.getLink());
            }
            return entries;
        } catch (FeedException | IOException e) {
            e.printStackTrace();
            logger.error("getSyndFeeds() Exception");
            return entries;
        }

    }

    @SecuredAction
    public static Result list() {
        User user = (User) ctx().args.get(SecureSocial.USER_KEY);
        return ok(feed.render(user, getTweets(), getSyndFeeds()));
    }
}
