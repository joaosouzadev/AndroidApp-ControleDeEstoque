package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductsContract;
import com.example.android.inventoryapp.data.ProductsContract.ProductsEntry;

public class ProductCursorAdapter extends CursorAdapter {

    private static final String LOG_TAG = ProductCursorAdapter.class.getSimpleName();

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // find views for item list layout
        TextView nameTextView = (TextView) view.findViewById(R.id.product_name);
        TextView priceTextView = (TextView) view.findViewById(R.id.product_price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.product_quantity);

        // find the columns of product attributes in database
        int nameColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_QUANTITY);

        // read the product attributes from the Cursor
        String productName = cursor.getString(nameColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);
        final String productQuantity = cursor.getString(quantityColumnIndex);

        final int productQuantityInt = cursor.getInt(quantityColumnIndex);
        final Uri productUri = ContentUris.withAppendedId(ProductsEntry.CONTENT_URI, cursor.getInt(cursor.getColumnIndexOrThrow(ProductsEntry._ID)));

        // update views with the attributes
        nameTextView.setText(productName);
        priceTextView.setText(productPrice);
        quantityTextView.setText(productQuantity);

        // sale button
        ImageView buyImageView = (ImageView) view.findViewById(R.id.product_buy);
        buyImageView.setImageResource(R.drawable.buy);

        buyImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (productQuantityInt > 0) {
                    int newQuantity = productQuantityInt - 1;

                    ContentValues values = new ContentValues();
                    values.put(ProductsEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);

                    context.getContentResolver().update(productUri, values, null, null);
                    Log.d(LOG_TAG, "URI for update: " + productUri);
                    Toast.makeText(context, context.getString(R.string.editor_update_product_successful), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, context.getString(R.string.editor_quantity_must_be_positive), Toast.LENGTH_SHORT).show();
                    }
            }
        });
    }
}
