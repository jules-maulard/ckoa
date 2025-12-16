package com.example.ckoa.views;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GeoShapeView extends View {

    private final Paint paint = new Paint();
    private final Path originalPath = new Path();
    private final Path drawPath = new Path();
    private final RectF bounds = new RectF();
    private final Matrix matrix = new Matrix();

    private boolean isDataLoaded = false;

    public GeoShapeView(Context context) {
        super(context);
        init();
    }

    public GeoShapeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    public void setGeoJson(String jsonString) {
        originalPath.reset();
        bounds.setEmpty();
        isDataLoaded = false;

        if (jsonString == null || jsonString.isEmpty()) {
            invalidate();
            return;
        }

        try {
            JSONObject feature = new JSONObject(jsonString);
            JSONObject geometry = feature.getJSONObject("geometry");
            String type = geometry.getString("type");
            JSONArray coordinates = geometry.getJSONArray("coordinates");

            if (type.equals("MultiPolygon")) {
                for (int i = 0; i < coordinates.length(); i++) {
                    parsePolygon(coordinates.getJSONArray(i));
                }
            } else if (type.equals("Polygon")) {
                parsePolygon(coordinates);
            }

            originalPath.computeBounds(bounds, true);
            isDataLoaded = true;

            requestLayout();
            invalidate();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parsePolygon(JSONArray polygon) throws JSONException {
        JSONArray ring = polygon.getJSONArray(0);
        if (ring.length() == 0) return;

        for (int i = 0; i < ring.length(); i++) {
            JSONArray coord = ring.getJSONArray(i);
            float x = (float) coord.getDouble(0);
            // Inversion de Y importante pour les coordonnées géographiques
            float y = (float) -coord.getDouble(1);

            if (i == 0) {
                originalPath.moveTo(x, y);
            } else {
                originalPath.lineTo(x, y);
            }
        }
        originalPath.close();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateTransformationMatrix();
    }

    private void updateTransformationMatrix() {
        if (!isDataLoaded || bounds.isEmpty()) return;

        float viewWidth = getWidth();
        float viewHeight = getHeight();

        float paddingX = viewWidth * 0.05f;
        float paddingY = viewHeight * 0.05f;

        float availableWidth = viewWidth - (paddingX * 2);
        float availableHeight = viewHeight - (paddingY * 2);

        double middleLatitude = Math.abs(bounds.centerY());
        float mapAspectCorrection = (float) (1.0 / Math.cos(Math.toRadians(middleLatitude)));

        float virtualMapWidth = bounds.width();
        float virtualMapHeight = bounds.height() * mapAspectCorrection;

        float scaleX = availableWidth / virtualMapWidth;
        float scaleY = availableHeight / virtualMapHeight;
        float finalScale = Math.min(scaleX, scaleY);

        matrix.reset();

        matrix.postTranslate(-bounds.left, -bounds.top);

        matrix.postScale(finalScale, finalScale * mapAspectCorrection);

        float finalWidth = virtualMapWidth * finalScale;
        float finalHeight = virtualMapHeight * finalScale;
        float offsetX = (viewWidth - finalWidth) / 2;
        float offsetY = (viewHeight - finalHeight) / 2;

        matrix.postTranslate(offsetX, offsetY);

        drawPath.reset();
        originalPath.transform(matrix, drawPath);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isDataLoaded) {
            canvas.drawPath(drawPath, paint);
        }
    }
}