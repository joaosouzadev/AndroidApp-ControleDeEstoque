package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductsContract.ProductsEntry;

import static com.example.android.inventoryapp.data.ProductsProvider.LOG_TAG;

public class ProductDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 0;

    private Uri mCurrentProductUri;

    private EditText mNameEditText;

    private EditText mDescriptionEditText;

    private EditText quantityTextView;

    private EditText mPriceEditText;

    private ImageView mImageView;

    private EditText mIdEditText;

    private Button mPlusButton;

    private Button mMinusButton;

    private Button mSaveButton;

    private Button mOrderButton;

    private ImageButton mDeleteButton;

    int productQuantityInt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        setTitle("Product Details");

        // Examine the intent that was used to launch this activity
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // Initialize a loader to read the product data from the database
        // and display the current values in the activity
        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

        // Find all relevant views that we will need to read user input from
        mIdEditText = (EditText) findViewById(R.id.edit_product_id);
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_product_description);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mImageView = (ImageView) findViewById(R.id.edit_product_image);
        quantityTextView = (EditText) findViewById(R.id.edit_product_quantity);

        // buttons
        mPlusButton = (Button) findViewById(R.id.plus_button);

        mPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String quantityString = quantityTextView.getText().toString().trim();
                productQuantityInt = Integer.parseInt(quantityString);
                productQuantityInt++;

                ContentValues values = new ContentValues();
                values.put(ProductsEntry.COLUMN_PRODUCT_QUANTITY, productQuantityInt);

                getContentResolver().update(mCurrentProductUri, values, null, null);
                Log.d(LOG_TAG, "URI for update: " + mCurrentProductUri);
                Toast.makeText(ProductDetailActivity.this, getString(R.string.editor_update_product_successful), Toast.LENGTH_SHORT).show();
            }
        });

        mMinusButton = (Button) findViewById(R.id.minus_button);

        mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String quantityString = quantityTextView.getText().toString().trim();
                productQuantityInt = Integer.parseInt(quantityString);
                if (productQuantityInt == 0){
                    Toast.makeText(ProductDetailActivity.this, getString(R.string.editor_quantity_must_be_positive), Toast.LENGTH_SHORT).show();
                } else {
                    productQuantityInt--;

                    ContentValues values = new ContentValues();
                    values.put(ProductsEntry.COLUMN_PRODUCT_QUANTITY, productQuantityInt);

                    getContentResolver().update(mCurrentProductUri, values, null, null);
                    Log.d(LOG_TAG, "URI for update: " + mCurrentProductUri);
                    Toast.makeText(ProductDetailActivity.this, getString(R.string.editor_update_product_successful), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSaveButton = (Button) findViewById(R.id.save_button);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String quantityString = quantityTextView.getText().toString().trim();
                productQuantityInt = Integer.parseInt(quantityString);
                if (productQuantityInt == 0){
                    Toast.makeText(ProductDetailActivity.this, getString(R.string.editor_quantity_save), Toast.LENGTH_SHORT).show();
                } else {
                    ContentValues values = new ContentValues();
                    values.put(ProductsEntry.COLUMN_PRODUCT_QUANTITY, productQuantityInt);

                    getContentResolver().update(mCurrentProductUri, values, null, null);
                    Log.d(LOG_TAG, "URI for update: " + mCurrentProductUri);
                    Toast.makeText(ProductDetailActivity.this, getString(R.string.editor_update_product_successful), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        mOrderButton = (Button) findViewById(R.id.order_button);

        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/html");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "supplier@gmail.com" });
                intent.putExtra(Intent.EXTRA_SUBJECT, "Request Order: " + mNameEditText.getText());
                intent.putExtra(Intent.EXTRA_TEXT, "Hi, I am contacting you because I need to order some more " + mNameEditText.getText() + "\n\nThank you :)");

                startActivity(Intent.createChooser(intent, "Send Email"));
            }
        });

        mDeleteButton = (ImageButton) findViewById(R.id.delete_button);

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();

            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the product table
        String[] projection = {
                ProductsEntry._ID,
                ProductsEntry.COLUMN_PRODUCT_NAME,
                ProductsEntry.COLUMN_PRODUCT_DESCRIPTION,
                ProductsEntry.COLUMN_PRODUCT_QUANTITY,
                ProductsEntry.COLUMN_PRODUCT_PRICE,
                ProductsEntry.COLUMN_PRODUCT_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {


        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int idColumnIndex = cursor.getColumnIndex(ProductsEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_NAME);
            int descriptionColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_DESCRIPTION);
            int quantityColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String id = cursor.getString(idColumnIndex);
            String name = cursor.getString(nameColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            byte[] imageStored = cursor.getBlob(imageColumnIndex);

            Bitmap bitmap = BitmapFactory.decodeByteArray(imageStored, 0, imageStored.length);

            // Update the views on the screen with the values from the database
            mIdEditText.setText(id);
            mNameEditText.setText(name);
            mDescriptionEditText.setText(description);
            quantityTextView.setText(Integer.toString(quantity));
            mPriceEditText.setText(Integer.toString(price));
            mImageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mDescriptionEditText.setText("");
        quantityTextView.setText("");
        mPriceEditText.setText("");
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this product?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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

    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, "Product removed", Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}
