package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.inventoryapp.data.ProductsContract.ProductsEntry;


public class ProductsProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = ProductsProvider.class.getSimpleName();

    private ProductsDbHelper mDbHelper;

    /**
     * URI matcher code for the content URI for the products table
     */
    private static final int PRODUCTS = 100;

    /**
     * URI matcher code for the content URI for a single product in the products table
     */
    private static final int PRODUCT_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(ProductsContract.CONTENT_AUTHORITY, ProductsContract.PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(ProductsContract.CONTENT_AUTHORITY, ProductsContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mDbHelper = new ProductsDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = database.query(ProductsEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = ProductsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // Cursor containing that row of the table.
                cursor = database.query(ProductsEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor.
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a productt into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertProduct(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(ProductsEntry.COLUMN_PRODUCT_NAME);
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Product requires a name");
        }

        // Check that the description is not null
        String description = values.getAsString(ProductsEntry.COLUMN_PRODUCT_DESCRIPTION);
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Product requires a description");
        }

        // Check that the quantity is not null and that it's greater than or equal to 0 kg
        Integer quantity = values.getAsInteger(ProductsEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity == null) {
            throw new IllegalArgumentException("Product requires valid price");
        }
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Product requires valid price");
        }

        // Check that the quantity is not null and that it's greater than or equal to 0 kg
        Integer price = values.getAsInteger(ProductsEntry.COLUMN_PRODUCT_PRICE);
        if (price == null) {
            throw new IllegalArgumentException("Product requires valid price");
        }
        if (price != null && quantity < 0) {
            throw new IllegalArgumentException("Product requires valid price");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new product with the given values
        long id = database.insert(ProductsEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        // uri: content://com.example.android.inventoryapp/products
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ProductsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = ProductsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ProductsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductsEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
