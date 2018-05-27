package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductsContract;
import com.example.android.inventoryapp.data.ProductsContract.ProductsEntry;

import static com.example.android.inventoryapp.data.ProductsContract.ProductsEntry._ID;

public class ProductDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the product data loader
     */
    private static final int EXISTING_PRODUCT_LOADER = 0;

    /**
     * Content URI for the existing product (null if it's a new product)
     */
    private Uri mCurrentProductUri;

    /**
     * EditText field to enter the product's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the product's description
     */
    private EditText mDescriptionEditText;

    /**
     * EditText field to enter the product quantity
     */
    private EditText mQuantityEditText;

    /**
     * EditText field to enter the pet's gender
     */
    private EditText mPriceEditText;

    private ImageView mImageView;

    private Button mPlusButton;

    private Button mMinusButton;

    private int productQuantity;

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
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_product_description);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mImageView = (ImageView) findViewById(R.id.edit_product_image);

        final Uri productUri = ContentUris.withAppendedId(ProductsContract.ProductsEntry.CONTENT_URI, 5);

        // buttons
        mPlusButton = (Button) findViewById(R.id.plus_button);
        mPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (productQuantity > 0) {
                    productQuantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
                    if (productQuantity > 0) {
                        productQuantity++;
                    } else {
                        Toast.makeText(ProductDetailActivity.this, "maior que 0", Toast.LENGTH_SHORT).show();
                    }
                    ContentValues values = new ContentValues();
                    values.put(ProductsContract.ProductsEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);

                    int rowsAffected = getContentResolver().update(productUri, values, null, null);
                    if (rowsAffected == 0) {
                        Toast.makeText(ProductDetailActivity.this, "fail", Toast.LENGTH_SHORT).show();
                    } else {
                        if (!(productQuantity < 1)) {
                            Toast.makeText(ProductDetailActivity.this, "success", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(ProductDetailActivity.this, "maior que 0", Toast.LENGTH_SHORT).show();
                }
                mQuantityEditText.setText(Integer.toString(productQuantity));
            }
        });

        mMinusButton = (Button) findViewById(R.id.minus_button);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the product table
        String[] projection = {
                ProductsEntry.COLUMN_PRODUCT_NAME,
                ProductsEntry.COLUMN_PRODUCT_DESCRIPTION,
                ProductsEntry.COLUMN_PRODUCT_QUANTITY,
                ProductsEntry.COLUMN_PRODUCT_PRICE,
                ProductsEntry.COLUMN_PRODUCT_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current pet
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
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_NAME);
            int descriptionColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_DESCRIPTION);
            int quantityColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            byte[] imageStored = cursor.getBlob(imageColumnIndex);

            Bitmap bitmap = BitmapFactory.decodeByteArray(imageStored, 0, imageStored.length);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mDescriptionEditText.setText(description);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Integer.toString(price));
            mImageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mDescriptionEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
    }
}
