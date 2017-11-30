package controllers;

import play.*;
import play.mvc.*;
import play.data.*;
import play.data.Form.*;
import play.mvc.Http.Context;

import play.mvc.Http.*;
import play.mvc.Http.MultipartFormData.FilePart;
import java.io.*;
import java.io.File;
import javax.activation.MimetypesFileTypeMap;
import play.Logger;

import views.html.manager.*;

// Import required classes
import java.util.ArrayList;
import java.util.List;

// Import models and security
import models.*;
import models.users.*;
import models.products.*;
import models.shopping.*;

import controllers.security.*;

// Authenticate user
@Security.Authenticated(Secured.class)
// Authorise user (check if admin)
@With(CheckIfManager.class)

public class ManagerProductCtrl extends Controller {
	
		static int countLowStock = 0;

    // Get a user - if logged in email will be set in the session
		private User getCurrentUser() {
		User u = User.getLoggedIn(session().get("email"));
		return u;
		}
    
    public Result index() {
        return redirect(routes.ManagerProductCtrl.listOrders());
    }

    public Result listOrders() {

        // Instantiate products, an Array list of products			
        List<ShopOrder> orders = new ArrayList<ShopOrder>();
            orders = ShopOrder.findAll();

        // Render the list products view, passwing parameters
        // categories and products lists
        // current user - if one is logged in
        return ok(listOrders.render(orders, getCurrentUser()));
    }

    public Result stockReport(Long cat) {

        List<Category> categories = Category.find.where().orderBy("name asc").findList();
        List<Product> products = new ArrayList<Product>();

        if (cat == 0) {
            // Get the list of ALL products
            products = Product.findAll();
        }
        else {
            // Get products for the selected category
            // Each category object contains a list of products
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).id == cat) {
                    products = categories.get(i).products;
                    break;
                }
            }
        }
        // Render the list products view, passwing parameters
        // categories and products lists
        // current user - if one is logged in
        return ok(stockReport.render(categories, products, getCurrentUser()));
    }

		public Result replenishStock(Long id, String quantity) {
        
        // Find the product

				if(quantity.equals("")){
					quantity = "0";
				}
        Product p = Product.find.byId(id);
				
				int a = Integer.parseInt(quantity);
				p.stock += a; 

				p.update();

				return redirect(routes.ManagerProductCtrl.stockReport(0));
		}

		public static Long getNumLowStock() {
        countLowStock = 0;
        // Find the product

        List<Product> products = new ArrayList<Product>();
            products = Product.findAll();
						
				for(int i = 0; i < products.size(); i++) {
					if(products.get(i).stock < 10){
						countLowStock++;
					} 
				}

				Long number = new Long(countLowStock);
				return number;
		}

    public Result editProfile(String email) {   
        // Instantiate a form object based on the Product class

				Manager m = Manager.findManager.ref(email);

        Form<Manager> editProfileForm = Form.form(Manager.class).fill(m);
        // Render the Add Product View, passing the form object
        return ok(editProfile.render(email, editProfileForm, User.getLoggedIn(session().get("email"))));
    }

    // Handle the form data when a new product is submitted
    public Result editProfileSubmit(String email) {

        // Create a product form object (to hold submitted data)
        // 'Bind' the object to the submitted form (this copies the filled form)
        Form<Manager> updateProfileForm = Form.form(Manager.class).bindFromRequest();	

        // Check for errors (based on Product class annotations)	
        if(updateProfileForm.hasErrors()) {
            // Display the form again
            return badRequest(editProfile.render(email, updateProfileForm, User.getLoggedIn(session().get("email"))));
        }
        
        Manager m = updateProfileForm.get();
        m.email = email;

        // Save product now to set id (needed to update manytomany)
        m.update();

        // Set a sucess message in temporary flash
        flash("success");

        // Redirect to the admin home
        return redirect(routes.ManagerProductCtrl.listOrders());
    }

    public Result deleteAccount(String email) {
        // Call delete method
		try{
        Manager.find.ref(email).delete();


        // Add message to flash session 
        flash("success", "Account has been deleted");
        // Redirect home
        return redirect(routes.Application.index());


		}catch(Exception e){

       return redirect(routes.ManagerProductCtrl.listOrders());
			}
		}

    public Result deleteOrder(Long id) {
        // Call delete method
				try{
        ShopOrder.find.ref(id).delete();

        // Add message to flash session 
        flash("success", "Order has been removed");
        // Redirect home
        return redirect(routes.ManagerProductCtrl.listOrders());

				}catch(Exception e){

        return redirect(routes.ManagerProductCtrl.listOrders());
			}
		}

}
