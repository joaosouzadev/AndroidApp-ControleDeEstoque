package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductsContract;

/**
 * Created by JOAO on 25-May-18.
 */

public class ProductCursorAdapter extends CursorAdapter {

    private int productQuantity;

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
        final TextView quantityTextView = (TextView) view.findViewById(R.id.product_quantity);
        ImageView buyImageView = (ImageView) view.findViewById(R.id.product_buy);

        // find the columns of product attributes in database
        int nameColumnIndex = cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_QUANTITY);

        // read the product attributes from the Cursor
        String productName = cursor.getString(nameColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);
        productQuantity = cursor.getInt(quantityColumnIndex);

        // update views with the attributes
        nameTextView.setText(productName);
        priceTextView.setText(productPrice);
        quantityTextView.setText(Integer.toString(productQuantity));
        buyImageView.setImageResource(R.drawable.buy);

        final int idColumn = cursor.getInt(cursor.getColumnIndex(ProductsContract.ProductsEntry._ID));
        final Uri productUri = ContentUris.withAppendedId(ProductsContract.ProductsEntry.CONTENT_URI, idColumn);

        buyImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!((productQuantity < 1))) {
                    productQuantity = Integer.parseInt(quantityTextView.getText().toString().trim());
                    if (!((productQuantity < 1))) {
                        productQuantity--;
                    } else {
                        Toast.makeText(context, context.getString(R.string.editor_quantity_must_be_positive), Toast.LENGTH_SHORT).show();
                    }
                    ContentValues values = new ContentValues();
                    values.put(ProductsContract.ProductsEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);
                    
                    int rowsAffected = context.getContentResolver().update(productUri, values, null, null);
                    if (rowsAffected == 0) {
                        Toast.makeText(context, context.getString(R.string.editor_update_product_failed), Toast.LENGTH_SHORT).show();
                    } else {
                        if (!(productQuantity < 1)) {
                            Toast.makeText(context, context.getString(R.string.editor_update_product_successful), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(context, context.getString(R.string.editor_quantity_must_be_positive), Toast.LENGTH_SHORT).show();
                }
                quantityTextView.setText(Integer.toString(productQuantity));
            }
        });
    }
}
