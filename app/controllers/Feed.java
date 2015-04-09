package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

/**
 * Created by hugh_sd on 4/8/15.
 */
public class Feed extends Controller {

    public static Result list() {
        return ok();
    }
}
