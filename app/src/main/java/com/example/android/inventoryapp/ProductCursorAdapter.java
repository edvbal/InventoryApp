package com.example.android.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

/**
 * Created by Edvinas on 25/06/2017.
 */

public class ProductCursorAdapter extends CursorAdapter {
    private MainActivity activity = new MainActivity();

    public ProductCursorAdapter(MainActivity context, Cursor c) {
        super(context, c, 0 /* flags */);
        activity = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final long id;
        final int quantityEditable;
        // Find fields to populate in inflated template
        TextView nameTextView = (TextView) view.findViewById(R.id.productName);
        TextView quantityTextView = (TextView) view.findViewById(R.id.productQuantityNumber);
        TextView priceTextView = (TextView) view.findViewById(R.id.productPriceNumber);
        ImageView pictureImageView = (ImageView) view.findViewById(R.id.productImage);
        Button buyButton = (Button) view.findViewById(R.id.buyProductButton);
        LinearLayout rootLayout = (LinearLayout) view.findViewById(R.id.rootLayout);

        // Extract properties from cursor
        id = cursor.getLong(cursor.getColumnIndex(ProductEntry._ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(
                ProductEntry.COLUMN_PRODUCT_NAME));
        final int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(
                ProductEntry.COLUMN_PRODUCT_QUANTITY));
        double price = cursor.getDouble(cursor.getColumnIndexOrThrow(
                ProductEntry.COLUMN_PRODUCT_PRICE));
        String pictureString = cursor.getString(cursor.getColumnIndexOrThrow(
                ProductEntry.COLUMN_PRODUCT_PICTURE));
        Uri imageUri = Uri.parse(pictureString);
        quantityEditable = quantity;
        // Update the Views with the extracted attributes from the current product
        nameTextView.setText(name);
        quantityTextView.setText(Integer.toString(quantity));
        priceTextView.setText(Double.toString(price));
        pictureImageView.setImageURI(imageUri);
        pictureImageView.invalidate();

        rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onProductClick(id);
            }
        });
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 0)
                    activity.onBuyClick(id, quantityEditable);
                else
                    Toast.makeText(activity, R.string.buy_out_of_stock, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
