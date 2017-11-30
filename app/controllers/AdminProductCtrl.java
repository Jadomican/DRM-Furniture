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

// File upload and image editing dependencies
import org.im4java.process.ProcessStarter;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;

import views.html.productAdmin.*;

// Import required classes
import java.util.ArrayList;
import java.util.List;

// Import models and security
import models.*;
import models.users.*;
import models.products.*;

import controllers.security.*;

// Authenticate user
@Security.Authenticated(Secured.class)
// Authorise user (check if admin)
@With(CheckIfAdmin.class)

public class AdminProductCtrl extends Controller {
	
    // Get a user - if logged in email will be set in the session
	private User getCurrentUser() {
		User u = User.getLoggedIn(session().get("email"));
		return u;
	}
    
    public Result index() {
        return redirect(routes.AdminProductCtrl.listProducts(0));
    }

    public Result listCustomers() {

        // Instantiate products, an Array list of products			
        List<Customer> customers = new ArrayList<Customer>();
        List<Manager> managers = new ArrayList<Manager>();
        List<Administrator> admins = new ArrayList<Administrator>();
            customers = Customer.findAll();
            managers = Manager.findAll();
            admins = Administrator.findAll();



        // Render the list products view, passwing parameters
        // categories and products lists
        // current user - if one is logged in
        return ok(listCustomers.render(customers, managers, admins, getCurrentUser()));
    }

    public Result deleteCustomer(String name) {

		if(Customer.find.ref(name).basket != null){				//clear basket before delete
        Customer.find.ref(name).basket.removeAllItems();
				Customer.find.ref(name).basket.update();
		}

        Customer.find.ref(name).delete();

        // Add message to flash session 
        flash("success", "User has been deleted");
        // Redirect home
        return redirect(routes.AdminProductCtrl.listCustomers());
    }

    public Result deleteManager(String name) {

        Manager.find.ref(name).delete();

        // Add message to flash session 
        flash("success", "User has been deleted");
        // Redirect home
        return redirect(routes.AdminProductCtrl.listCustomers());
    }

    public Result listProducts(Long cat) {
        // Get list of categories in ascending order
        List<Category> categories = Category.find.where().orderBy("name asc").findList();
        // Instantiate products, an Array list of products			
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

        return ok(listProducts.render(categories, products, getCurrentUser()));
    }
   
    // Display an empty form in the view
    public Result addProduct() {   
        // Instantiate a form object based on the Product class
        Form<Product> addProductForm = Form.form(Product.class);
        // Render the Add Product View, passing the form object
        return ok(addProduct.render(addProductForm, getCurrentUser()));
    }

    // Handle the form data when a new product is submitted
    public Result addProductSubmit() {

        String saveImageMsg;

        // Create a product form object (to hold submitted data)
        // 'Bind' the object to the submitted form (this copies the filled form)
        Form<Product> newProductForm = Form.form(Product.class).bindFromRequest();	

        // Check for errors (based on Product class annotations)	
        if(newProductForm.hasErrors()) {
            // Display the form again
            return badRequest(addProduct.render(newProductForm, getCurrentUser()));
        }
     
        Product newProduct = newProductForm.get();
        
        // Save product now to set id (needed to update manytomany)
        newProduct.save();
        
        // Get category ids (checked boxes from form)
        // Find category objects and set categories list for this product
        for (Long cat : newProduct.catSelect) {
            newProduct.categories.add(Category.find.byId(cat));
        }

        // Update the new Product to save categories
        newProduct.update();

        // Get image data
        MultipartFormData data = request().body().asMultipartFormData();
        FilePart image = data.getFile("upload");
        // Save the image file
        saveImageMsg = saveFile(newProductForm.get().id, image);
        saveImageMsg = saveFileThumbnail(newProductForm.get().id, image);

        flash("success", "Product " + newProduct.name + " has been created" + " " + saveImageMsg);
            
        // Redirect to the admin home
        return redirect(routes.AdminProductCtrl.index());
    }
        
    // Update a product by ID (edit button)
    public Result updateProduct(Long id) {
        // Retrieve the product by id
        Product p = Product.find.byId(id);
        // Create a form based on the Product class and fill using p
        Form<Product> productForm = Form.form(Product.class).fill(p);
        // Render the updateProduct view
        // pass the id and form as parameters
        return ok(updateProduct.render(id, productForm, User.getLoggedIn(session().get("email"))));		
    }

    // Handle the form data when an updated product is submitted
    public Result updateProductSubmit(Long id) {

        String saveImageMsg;

        // Create a product form object (to hold submitted data)
        // 'Bind' the object to the submitted form (this copies the filled form)
        Form<Product> updateProductForm = Form.form(Product.class).bindFromRequest();	

        // Check for errors (based on Product class annotations)	
        if(updateProductForm.hasErrors()) {
            // Display the form again
            return badRequest(updateProduct.render(id, updateProductForm, getCurrentUser()));
        }
        
        // Update the Product (using its ID to select the existing object))
        Product p = updateProductForm.get();
        p.id = id;
        
        // Get category ids (checked boxes from form)
        // Find category objects and set categories list for this product
        List<Category> newCats = new ArrayList<Category>();
        for (Long cat : p.catSelect) {
            newCats.add(Category.find.byId(cat));
        }
        p.categories = newCats;
        
        // update (save) this product            
        p.update();

        // Get image data
        MultipartFormData data = request().body().asMultipartFormData();
        FilePart image = data.getFile("upload");
        //saveImageMsg = saveFileThumbnail(p.id, image);
        saveImageMsg = saveFile(p.id, image);

        // Add a success message to the flash session
        flash("success", "Product " + updateProductForm.get().name + " has been updated" + " " + saveImageMsg);
            
        // Return to admin home
        return redirect(routes.AdminProductCtrl.index());
    }

    public Result registerAdmin() {   
        // Instantiate a form object based on the class
        Form<Administrator> addUserForm = Form.form(Administrator.class);
        // Render the Add Product View, passing the form object
        return ok(registerAdmin.render(addUserForm, User.getLoggedIn(session().get("email"))));
    }

    public Result registerManager() {   

        Form<Manager> addUserForm = Form.form(Manager.class);
        // Render the Add Product View, passing the form object
        return ok(registerManager.render(addUserForm, User.getLoggedIn(session().get("email"))));
    }

    public Result addAdminSubmit() {
        Form<Administrator> newUserForm = Form.form(Administrator.class).bindFromRequest();	
        // Create a product form object (to hold submitted data)
        // 'Bind' the object to the submitted form (this copies the filled form)
        // Check for errors (based on Product class annotations)	
				try{

        if(newUserForm.hasErrors()) {
            // Display the form again
            return badRequest(registerAdmin.render(newUserForm, User.getLoggedIn(session().get("email"))));
        }
     
        Administrator newAdmin = newUserForm.get();
        
        // Save product now to set id (needed to update manytomany)
        newAdmin.save();

        flash("success", "User registered!");
				session().clear();
				
        return redirect(controllers.security.routes.LoginCtrl.login());
				}catch(Exception e){
        flash("success", "This email is already in use!");
            return badRequest(registerAdmin.render(newUserForm, User.getLoggedIn(session().get("email"))));
    		}
			}

    public Result addManagerSubmit() {

        // Create a product form object (to hold submitted data)
        // 'Bind' the object to the submitted form (this copies the filled form)
        Form<Manager> newUserForm = Form.form(Manager.class).bindFromRequest();	
				try{
        // Check for errors (based on Product class annotations)	
        if(newUserForm.hasErrors()) {
            // Display the form again
            return badRequest(registerManager.render(newUserForm, User.getLoggedIn(session().get("email"))));
        }
     
        Manager newManager = newUserForm.get();
        
        // Save product now to set id (needed to update manytomany)
        newManager.save();

        // Set a sucess message in temporary flash
        flash("success", "User registered!");
				session().clear();
        // Redirect to the admin home
        return redirect(controllers.security.routes.LoginCtrl.login());
				}catch(Exception e){
        flash("success", "This email is already in use!");
            return badRequest(registerManager.render(newUserForm, User.getLoggedIn(session().get("email"))));
				}
    }

    public Result editProfile(String email) {   
        // Instantiate a form object based on the Product class

        List<Administrator> admins = new ArrayList<Administrator>();

        admins = Administrator.findAll();

				Administrator a = Administrator.findAdmin.ref(email);

        Form<Administrator> editProfileForm = Form.form(Administrator.class).fill(a);
        // Render the Add Product View, passing the form object
        return ok(editProfile.render(email, admins ,editProfileForm, User.getLoggedIn(session().get("email"))));
    }

    public Result editProfileSubmit(String email) {

        // Create a product form object (to hold submitted data)
        // 'Bind' the object to the submitted form (this copies the filled form)
        Form<Administrator> updateProfileForm = Form.form(Administrator.class).bindFromRequest();	

        // Check for errors (based on Product class annotations)	
        if(updateProfileForm.hasErrors()) {
            // Display the form again
        List<Administrator> admins = new ArrayList<Administrator>();

            admins = Administrator.findAll();
            return badRequest(editProfile.render(email, admins, updateProfileForm, User.getLoggedIn(session().get("email"))));
        }
        
        Administrator a = updateProfileForm.get();
        a.email = email;

        // Save product now to set id (needed to update manytomany)
        a.update();
        flash("success");

        // Redirect to the admin home
        return redirect(routes.AdminProductCtrl.listProducts(0));
    }


    public Result deleteAccount(String email) {
        // Call delete method
				try{
        Administrator.find.ref(email).delete();

        // Add message to flash session 
        flash("success", "Account has been deleted");
        // Redirect home
        return redirect(routes.Application.index());

				}catch(Exception e){

        return redirect(routes.AdminProductCtrl.listProducts(0));
				}
	}


    // Delete Product
    public Result deleteProduct(Long id) {
				try{
        // Call delete method
        Product.find.ref(id).delete();

        flash("success", "Product has been deleted");
        // Redirect home
        return redirect(routes.AdminProductCtrl.index());
				}catch(Exception e){
        flash("warning", "This item is part of a pending order and cannot be deleted");
        return redirect(routes.AdminProductCtrl.listProducts(0));
				}
    }
    
    // Save an image file
    public String saveFile(Long id, FilePart image) {
        if (image != null) {
            // Get mimetype from image
            String fileName = image.getFilename();

						String extension = "";

						String mimeType = image.getContentType();
            // Check if uploaded file is an image
            if (mimeType.startsWith("image/")) {
                // Create file from uploaded image

							int i = fileName.lastIndexOf('.');
							if (i >= 0) {
							extension = fileName.substring(i+1);
							}

							File file = image.getFile();
	
							file.renameTo(new File("public/images/productImages", id + "." + extension));
                return "/ and image saved";
            }
        }
        return "image file missing";	
    } 

    // Save an image file
    public String saveFileThumbnail(Long id, FilePart image) {
        if (image != null) {
            // Get mimetype from image
            String fileName = image.getFilename();

						String extension = "";

						String mimeType = image.getContentType();
            // Check if uploaded file is an image
            if (mimeType.startsWith("image/")) {
                // Create file from uploaded image

							int i = fileName.lastIndexOf('.');
							if (i >= 0) {
							extension = fileName.substring(i+1);
							}

							File file = image.getFile();
	
							file.renameTo(new File("public/images/productImages/thumbnails", id + "." + extension)); //here
                return "/ and image saved";
            }
        }
        return "image file missing";	
    } 
}
