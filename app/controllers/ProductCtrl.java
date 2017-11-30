package controllers;

import play.*;
import play.mvc.*;
import play.data.*;
import play.data.Form.*;
import play.mvc.Http.Context;

import java.util.ArrayList;
import java.util.List;

import views.html.*;

// Import models
import models.*;
import models.users.*;
import models.products.*;

// Import security controllers
import controllers.security.*;

public class ProductCtrl extends Controller {

    // Get a user - if logged in email will be set in the session
	public User getCurrentUser() {
		User u = User.getLoggedIn(session().get("email"));
		return u;
	}
    	    
    public Result index() {
		return redirect(routes.ProductCtrl.listProducts(0));
    }

		// Get a list of products
    // If cat parameter is 0 then return all products, else get by cat
    public Result listProducts(Long cat) {
        // Get list of all categories in ascending order
        List<Category> categories = Category.find.where().orderBy("name asc").findList();
        // Instantiate products, an Array list of products			
        List<Product> products = new ArrayList<Product>();
    
				String catName = "";
        if (cat == 0) {
            // Get the list of ALL products
            products = Product.findAll();
						catName = "All Categories";

        }
        else {
            // Get products for the selected category
            // Each category object contains a list of products

            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).id == cat) {
                    products = categories.get(i).products;
										catName = categories.get(i).name;		//set  category name
                    break;
                }
            }
        }
        // passing parameters to scala page
        // current user - if one is logged in
        return ok(listProducts.render(categories, catName, products, getCurrentUser()));
    }
}
