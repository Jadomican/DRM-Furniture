package controllers;

import play.*;
import play.mvc.*;
import play.data.*;
import play.data.Form.*;
import play.mvc.Http.Context;

import java.util.*;

import views.html.*;

// Import models
import models.*;
import models.users.*;
import models.products.*;
import models.shopping.*;

// Import security controllers
import controllers.security.*;

public class Application extends Controller {
	
    public Result index() {
				//List<Product> products = new ArrayList<Product>();
				//products = Product.findAll();
        return ok(index.render(Form.form(Login.class), User.getLoggedIn(session().get("email"))));
    }

		public Result about() {
        return ok(about.render(User.getLoggedIn(session().get("email"))));
		}

		public Result contact() {
        return ok(contact.render(User.getLoggedIn(session().get("email"))));
    }

    public Result product(Long id) {

    	try {

				List<Product> products = new ArrayList<Product>();
				products = Product.findAll();

				Product p = Product.find.byId(id);

        return ok(product.render(p, User.getLoggedIn(session().get("email")), products));


    	} catch(Exception e) {

        return ok(error.render("This product may not exist", User.getLoggedIn(session().get("email"))));
			}

    }

    public Result listOrders() {

				try{	
        	List<ShopOrder> orders = new ArrayList<ShopOrder>();
        	List<ShopOrder> ordersCust = new ArrayList<ShopOrder>();
        	Customer customer = Customer.getLoggedIn(session().get("email"));
        	orders = ShopOrder.findAll();

					for(ShopOrder o : orders){
						if(o.customer.email.equals(customer.email)){
							ordersCust.add(o);
						}
					}

        return ok(listOrders.render(ordersCust,customer, User.getLoggedIn(session().get("email"))));
				}catch(Exception e){
        return redirect(controllers.security.routes.LoginCtrl.login());
				}
    }

    public Result register() {   
        // Instantiate a form object based on the Product class
        Form<Customer> addUserForm = Form.form(Customer.class);
        // passing the form object
        return ok(register.render(addUserForm, User.getLoggedIn(session().get("email"))));
    }

    // Handle the form data when a new product is submitted
    public Result addUserSubmit() {
        Form<Customer> newUserForm = Form.form(Customer.class).bindFromRequest();	
				try{
        // Create a product form object (to hold submitted data)
        // 'Bind' the object to the submitted form (this copies the filled form)


        // Check for errors (based on Product class annotations)	
        if(newUserForm.hasErrors()) {
            // Display the form again
            return badRequest(register.render(newUserForm, User.getLoggedIn(session().get("email"))));
        }
     
        Customer newCustomer = newUserForm.get();
        
        // Save product now to set id (needed to update manytomany)
        newCustomer.save();

        // Set a sucess message in temporary flash
        flash("success", "User registered!");

        // Redirect to the admin home
        return redirect(controllers.security.routes.LoginCtrl.login());
				}catch(Exception e){
        flash("success", "This email is already in use!");
            return badRequest(register.render(newUserForm, User.getLoggedIn(session().get("email"))));
				}
    }

    public Result editProfile(String email) {   
        // Instantiate a form object based on the Product class

				Customer c = Customer.find.ref(email);

        Form<Customer> editProfileForm = Form.form(Customer.class).fill(c);
        // Render the Add Product View, passing the form object
        return ok(editProfile.render(email, editProfileForm, User.getLoggedIn(session().get("email"))));
    }

    // Handle the form data when a new product is submitted
    public Result editProfileSubmit(String email) {

        // Create a product form object (to hold submitted data)
        // 'Bind' the object to the submitted form (this copies the filled form)
        Form<Customer> updateProfileForm = Form.form(Customer.class).bindFromRequest();	

        // Check for errors (based on Product class annotations)	
        if(updateProfileForm.hasErrors()) {
            // Display the form again
            return badRequest(editProfile.render(email, updateProfileForm, User.getLoggedIn(session().get("email"))));
        }
        
        Customer c = updateProfileForm.get();
        c.email = email;

        // Save product now to set id (needed to update manytomany)
        c.update();

        // Set a sucess message in temporary flash
        flash("success");

        // Redirect to the admin home
        return redirect(routes.Application.index());
    }

		public Result resultPage(String arg){

        List<Product> products = new ArrayList<Product>();
        List<Product> resultList = new ArrayList<Product>();
				products = Product.findAll(); //search through all products

				for(Product p : products) {

					if(p.name.toLowerCase().contains(arg.toLowerCase()) || p.description.toLowerCase().contains(arg.toLowerCase())){
						resultList.add(p);
					}
				}

			return ok(resultPage.render(arg, resultList, User.getLoggedIn(session().get("email"))));
		}

		public Result error(String e){

			return ok(error.render(e, User.getLoggedIn(session().get("email"))));
		}

    public Result deleteAccount(String email) {
        // Call delete method
				try{
        Customer.find.ref(email).delete();

        // Add message to flash session 
        flash("success", "Account has been deleted");
        // Redirect home
        return redirect(routes.Application.index());

				}catch(Exception e){

        return ok(error.render("Be sure to clear your cart first!", User.getLoggedIn(session().get("email"))));
			}
		}

}



