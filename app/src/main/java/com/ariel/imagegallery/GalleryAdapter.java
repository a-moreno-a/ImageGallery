package com.ariel.imagegallery;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.downloadtool.arielmoreno.downloadtool.ActionDataAdapter;
import com.downloadtool.arielmoreno.downloadtool.DistributedContentDownloader;

import java.util.ArrayList;

/**
 * Created by Ariel on 1/25/2017.
 */

/**
 * This class extends the recyclerview class, in order to visualize data in an efficient way
 */
public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryView> {
    public ArrayList<Integer> images = new ArrayList<Integer>();
    DistributedContentDownloader dm;
    private Context context;

    /**
     * Constructor initialized all the required varaibles
     *
     * @param context this is the main activity
     * @param dm      the DistributedContentDownloader used to get the items
     */
    public GalleryAdapter(Context context, DistributedContentDownloader dm) {
        this.context = context;
        this.dm = dm;
    }

    @Override
    public GalleryView onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
        GalleryView galleryView = new GalleryView(layoutView);

        return galleryView;
    }

    /**
     * Method used to make rounded bitmaps
     */
    private Bitmap getRoundedCornerBitmap(Context context, Bitmap input, int pixels, int w, int h, boolean squareTL, boolean squareTR, boolean squareBL, boolean squareBR) {

        Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final float densityMultiplier = context.getResources().getDisplayMetrics().density;

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, w, h);
        final RectF rectF = new RectF(rect);

        //make sure that our rounded corner is scaled appropriately
        final float roundPx = pixels * densityMultiplier;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);


        //draw rectangles over the corners we want to be square
        if (squareTL) {
            canvas.drawRect(0, h / 2, w / 2, h, paint);
        }
        if (squareTR) {
            canvas.drawRect(w / 2, h / 2, w, h, paint);
        }
        if (squareBL) {
            canvas.drawRect(0, 0, w / 2, h / 2, paint);
        }
        if (squareBR) {
            canvas.drawRect(w / 2, 0, w, h / 2, paint);
        }


        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(input, 0, 0, paint);

        return output;
    }

    /**
     * When the a new item is needed perform a download
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(GalleryView holder, int position) {
        ((GalleryActivity) context).IncPendingCount();

        dm.DownloadFilesRange(images.get(position), images.get(position), new ImageAdapterAction(holder));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    /**
     * Used to insert element at the beginning of the collection
     *
     * @param n index within the json content
     */
    public void insert(int n) {
        images.add(0, n);
        notifyItemRangeInserted(0, 1);
    }

    /**
     * Class used to define the template taht is going to be used for the items
     */
    static class GalleryView extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        LinearLayout progressFrame;

        public GalleryView(View itemView) {
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.img);
            textView = (TextView) itemView.findViewById(R.id.img_name);
            progressFrame = (LinearLayout) itemView.findViewById(R.id.loadingPanel);

            DisplayMetrics displaymetrics = new DisplayMetrics();
            ((Activity) itemView.getContext()).getWindowManager()
                    .getDefaultDisplay()
                    .getMetrics(displaymetrics);
            progressFrame.setLayoutParams(new LinearLayout.LayoutParams(Math.round(((float) (displaymetrics.widthPixels)) / 2.5f), LinearLayout.LayoutParams.WRAP_CONTENT));
            //Text size set as a percent of screen width for simplicity
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, ((float) (displaymetrics.widthPixels)) * 0.04f);
        }
    }

    /**
     * Adapter used to convert byte[] ito bitmap and set content on the view
     */
    class ImageAdapterAction implements ActionDataAdapter {
        GalleryView holder;

        public ImageAdapterAction(GalleryView holder) {
            this.holder = holder;
        }

        @Override
        public void PerformOperation(byte[] data) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Bitmap src = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            Bitmap rounded = getRoundedCornerBitmap(context, src, 5, src.getWidth(), src.getHeight(), true, true, false, false);

            holder.imageView.setMaxWidth(src.getWidth());
            holder.imageView.setMaxHeight(src.getHeight());
            holder.imageView.setImageBitmap(rounded);
            holder.textView.setText("John Doe");
            holder.progressFrame.setVisibility(View.GONE);
            holder.textView.setVisibility(View.VISIBLE);
            src.recycle();

            ((GalleryActivity) context).DecPendingCount();
        }
    }
}
