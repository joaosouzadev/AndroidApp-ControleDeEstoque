package com.example.android.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.media.Image;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventoryapp.data.ProductsContract;

/**
 * Created by JOAO on 25-May-18.
 */

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // find views for item list layout
        TextView nameTextView = (TextView) view.findViewById(R.id.product_name);
        TextView priceTextView = (TextView) view.findViewById(R.id.product_price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.product_quantity);
        ImageView buyImageView = (ImageView) view.findViewById(R.id.product_buy);

        // find the columns of product attributes in database
        int nameColumnIndex = cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_QUANTITY);

        // read the product attributes from the Cursor
        String productName = cursor.getString(nameColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);
        String productQuantity = cursor.getString(quantityColumnIndex);

        // update views with the attributes
        nameTextView.setText(productName);
        priceTextView.setText(productPrice);
        quantityTextView.setText(productQuantity);
        buyImageView.setImageResource(R.drawable.buy);
    }
}
