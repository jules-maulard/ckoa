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
    // Path original (coordonnées géographiques)
    private final Path originalPath = new Path();
    // Path transformé (coordonnées écran) pour l'affichage
    private final Path drawPath = new Path();
    // Limites du path original
    private final RectF bounds = new RectF();
    // Matrice pour les transformations (mise à l'échelle, centrage)
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

            // Calculer les limites une seule fois après le chargement
            originalPath.computeBounds(bounds, true);
            isDataLoaded = true;

            // Demander un nouveau dessin
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
        // Recalculer la transformation si la taille de la vue change
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

        // --- CORRECTION DE PROJECTION ---
        // On récupère la latitude moyenne du pays (le centre vertical)
        // Note: bounds.centerY() est négatif car on a inversé Y au parsing, on prend la valeur absolue
        double middleLatitude = Math.abs(bounds.centerY());

        // Formule magique : facteur d'étirement vertical = 1 / cos(latitude)
        // Cela redonne au pays sa "vraie" forme visuelle
        float mapAspectCorrection = (float) (1.0 / Math.cos(Math.toRadians(middleLatitude)));
        // --------------------------------

        // On calcule la taille "virtuelle" du pays une fois corrigé
        float virtualMapWidth = bounds.width();
        float virtualMapHeight = bounds.height() * mapAspectCorrection;

        // On calcule l'échelle pour faire rentrer cette taille virtuelle dans l'écran
        float scaleX = availableWidth / virtualMapWidth;
        float scaleY = availableHeight / virtualMapHeight;
        float finalScale = Math.min(scaleX, scaleY);

        matrix.reset();

        // 1. On remet l'origine en haut à gauche
        matrix.postTranslate(-bounds.left, -bounds.top);

        // 2. On applique l'échelle ET la correction de projection sur l'axe Y
        matrix.postScale(finalScale, finalScale * mapAspectCorrection);

        // 3. On centre le tout
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