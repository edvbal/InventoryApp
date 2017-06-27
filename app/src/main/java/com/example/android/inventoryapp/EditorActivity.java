package com.example.android.inventoryapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

/**
 * Created by Edvinas on 25/06/2017.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int EXISTING_PRODUCT_LOADER = 0;
    private Uri currentProductUri;
    private Uri imageUri;
    private ImageView imageViewPicture;
    private TextView clickToAddPic;
    private EditText editTextName;
    private EditText editTextPrice;
    private EditText editTextSupplierName;
    private EditText editTextSupplierEmail;
    private Button increaseQuantity;
    private Button decreaseQuantity;
    private TextView textViewQuantity;
    private int quantity;
    private boolean productHasChanged = false;
    private boolean canProductBeSaved = false;
    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the productHasChanged boolean to true.
     */
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            productHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        Intent intent = getIntent();
        currentProductUri = intent.getData();
        imageViewPicture = (ImageView) findViewById(R.id.editPicture);
        textViewQuantity = (TextView) findViewById(R.id.editQuantityNumber);
        clickToAddPic = (TextView) findViewById(R.id.clickToAddPicture);
        editTextName = (EditText) findViewById(R.id.nameEditText);
        editTextPrice = (EditText) findViewById(R.id.priceEditText);
        editTextSupplierName = (EditText) findViewById(R.id.supplierNameEditText);
        editTextSupplierEmail = (EditText) findViewById(R.id.supplierEmailEditText);
        increaseQuantity = (Button) findViewById(R.id.editIncreaseQuantity);
        decreaseQuantity = (Button) findViewById(R.id.editDecreaseQuantity);
        increaseQuantityButtonPress();
        decreaseQuantityButtonPress();
        imageViewPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trySelector();
                productHasChanged = true;
            }
        });

        if (currentProductUri == null) {
            // This is a new product, so change the app bar to say "Add a Product"
            setTitle(getString(R.string.addAProduct));
            clickToAddPic.setVisibility(View.VISIBLE);

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit Product"
            setTitle(getString(R.string.editProduct));
            clickToAddPic.setVisibility(View.GONE);

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getSupportLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

            imageViewPicture.setOnTouchListener(touchListener);
            editTextName.setOnTouchListener(touchListener);
            editTextPrice.setOnTouchListener(touchListener);
            editTextSupplierName.setOnTouchListener(touchListener);
            editTextSupplierEmail.setOnTouchListener(touchListener);
        }
    }

    private void decreaseQuantityButtonPress() {
        decreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity == 0)
                    Toast.makeText(EditorActivity.this, R.string.quantityAlreadyZero, Toast.LENGTH_SHORT).show();
                else {
                    quantity--;
                    displayQuantity();
                }
            }
        });
    }

    private void increaseQuantityButtonPress() {
        increaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity++;
                displayQuantity();
            }
        });
    }

    public void displayQuantity() {
        textViewQuantity.setText(String.valueOf(quantity));
    }

    public void trySelector() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }
        openSelector();
    }

    private void openSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.selectPicture)), 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openSelector();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                imageUri = data.getData();
                clickToAddPic.setVisibility(View.GONE);
                imageViewPicture.setImageURI(imageUri);
                imageViewPicture.invalidate();
            }
        }
    }

    private boolean saveProduct() {
        String nameString = editTextName.getText().toString().trim();
        String priceString = editTextPrice.getText().toString().trim();
        String supplierNameString = editTextSupplierName.getText().toString().trim();
        String supplierEmailString = editTextSupplierEmail.getText().toString().trim();
        String quantityString = textViewQuantity.getText().toString();

        // Check if this is supposed to be a new product
        // and check if all the fields in the editor are blank
        ContentValues contentValues = new ContentValues();

        contentValues.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityString);

        if (currentProductUri == null &&
                TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierEmailString) && imageUri == null) {
            canProductBeSaved = true;
            return canProductBeSaved;
        }
        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, R.string.insertProductNameToast, Toast.LENGTH_SHORT).show();
            canProductBeSaved = false;
            return canProductBeSaved;
        }
        contentValues.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);

        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, R.string.insertPriceToast, Toast.LENGTH_SHORT).show();
            canProductBeSaved = false;
            return canProductBeSaved;
        }
        contentValues.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceString);

        if (TextUtils.isEmpty(supplierNameString)) {
            Toast.makeText(this, R.string.insertSupplierNameToast, Toast.LENGTH_SHORT).show();
            canProductBeSaved = false;
            return canProductBeSaved;
        }
        contentValues.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supplierNameString);

        if (TextUtils.isEmpty(supplierEmailString)) {
            Toast.makeText(this, R.string.insertSupplierEmail, Toast.LENGTH_SHORT).show();
            canProductBeSaved = false;
            return canProductBeSaved;
        }
        contentValues.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL, supplierEmailString);

        if (imageUri == null) {
            Toast.makeText(this, R.string.productPictureNotSet, Toast.LENGTH_SHORT).show();
            canProductBeSaved = false;
            return canProductBeSaved;
        }
        contentValues.put(ProductEntry.COLUMN_PRODUCT_PICTURE, imageUri.toString());

        if (currentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, contentValues);
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, R.string.editorInsertFailed,
                        Toast.LENGTH_SHORT).show();
            } else
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, R.string.editorInsertSUcc,
                        Toast.LENGTH_SHORT).show();
        } else {
            int rowsAffected = getContentResolver().update(currentProductUri, contentValues, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, R.string.editUpdateFailed,
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, R.string.editUpdateProductSucc,
                        Toast.LENGTH_SHORT).show();

            }
        }
        canProductBeSaved = true;
        return canProductBeSaved;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (currentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database
                if (saveProduct())
                    finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            case R.id.order_more:
                orderMore();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!productHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void orderMore() {
        Intent intent = new Intent(android.content.Intent.ACTION_SENDTO);
        intent.setType("text/plain");
        intent.setData(Uri.parse("mailto:" + editTextSupplierEmail.getText().toString().trim()));
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "New Order");
        String message = "We want to order n of " + editTextName.getText().toString().trim();
        intent.putExtra(android.content.Intent.EXTRA_TEXT, message);
        startActivity(intent);
    }

    /**
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.deleteThisProduct);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (currentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the currentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, R.string.errorDeletingProduct,
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, R.string.productDeleted,
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!productHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.discardChanges);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keepEditing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PICTURE,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL
        };
        return new CursorLoader(this,
                currentProductUri,
                projection,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1)
            return;
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int pictureColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PICTURE);
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int supplierEmailColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);

            // Extract out the value from the Cursor for the given column index
            String imageUriString = cursor.getString(pictureColumnIndex);
            String name = cursor.getString(nameColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            quantity = cursor.getInt(quantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierEmail = cursor.getString(supplierEmailColumnIndex);

            // Update the views on the screen with the values from the database
            editTextName.setText(name);
            editTextPrice.setText(price);
            editTextSupplierName.setText(supplierName);
            editTextSupplierEmail.setText(supplierEmail);
            textViewQuantity.setText(Integer.toString(quantity));
            imageUri = Uri.parse(imageUriString);
            imageViewPicture.setImageURI(imageUri);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        editTextName.setText("");
        editTextPrice.setText("");
        editTextSupplierName.setText("");
        editTextSupplierEmail.setText("");
        textViewQuantity.setText("");
        imageViewPicture.setImageResource(0);
    }
}
