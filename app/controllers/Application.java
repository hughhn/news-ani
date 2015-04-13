package controllers;

import play.*;
import play.mvc.*;

import play.libs.F;
import securesocial.core.BasicProfile;
import securesocial.core.RuntimeEnvironment;
import securesocial.core.java.SecureSocial;
import securesocial.core.java.SecuredAction;
import securesocial.core.java.UserAwareAction;
import services.User;
import views.html.*;

public class Application extends Controller {
    public static Logger.ALogger logger = Logger.of("application.controllers.Application");
    private RuntimeEnvironment<User> env;

    /**
     * A constructor needed to get a hold of the environment instance.
     * This could be injected using a DI framework instead too.
     *
     * @param env
     */
    public Application(RuntimeEnvironment<User> env) {
        this.env = env;
    }

    public static User getCurrentUser() {
        return (User) ctx().args.get(SecureSocial.USER_KEY);
    }

    @SecuredAction
    public Result index() {
        if(logger.isDebugEnabled()){
            logger.debug("access granted to index");
        }
        return ok(index.render(getCurrentUser(), SecureSocial.env()));
    }

    @SecuredAction
    public Result linkResult() {
        User current = getCurrentUser();
        return ok(linkResult.render(current, current.identities));
    }

    @UserAwareAction
    public Result userAware() {
        User demoUser = getCurrentUser();
        String userName ;
        if ( demoUser != null ) {
            BasicProfile user = demoUser.main;
            if ( user.firstName().isDefined() ) {
                userName = user.firstName().get();
            } else if ( user.fullName().isDefined()) {
                userName = user.fullName().get();
            } else {
                userName = "authenticated user";
            }
        } else {
            userName = "guest";
        }
        return ok("Hello " + userName + ", you are seeing a public page");
    }

    /**
     * Sample use of SecureSocial.currentUser. Access the /current-user to test it
     */
    public F.Promise<Result> currentUser() {
        return SecureSocial.currentUser(env).map(new F.Function<Object, Result>() {
            @Override
            public Result apply(Object maybeUser) throws Throwable {
                String id;

                if (maybeUser != null) {
                    User user = (User) maybeUser;
                    id = user.main.userId();
                } else {
                    id = "not available. Please log in.";
                }
                return ok("your id is " + id);
            }
        });
    }
}
