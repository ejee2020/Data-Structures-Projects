package bearmaps.proj2c.server.handler.impl;

import bearmaps.proj2c.AugmentedStreetMapGraph;
import bearmaps.proj2c.server.handler.APIRouteHandler;
import spark.Request;
import spark.Response;
import bearmaps.proj2c.utils.Constants;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static bearmaps.proj2c.utils.Constants.SEMANTIC_STREET_GRAPH;
import static bearmaps.proj2c.utils.Constants.ROUTE_LIST;

/**
 * Handles requests from the web browser for map images. These images
 * will be rastered into one large image to be displayed to the user.
 * @author rahul, Josh Hug, _________
 */
public class RasterAPIHandler extends APIRouteHandler<Map<String, Double>, Map<String, Object>> {

    /**
     * Each raster request to the server will have the following parameters
     * as keys in the params map accessible by,
     * i.e., params.get("ullat") inside RasterAPIHandler.processRequest(). <br>
     * ullat : upper left corner latitude, <br> ullon : upper left corner longitude, <br>
     * lrlat : lower right corner latitude,<br> lrlon : lower right corner longitude <br>
     * w : user viewport window width in pixels,<br> h : user viewport height in pixels.
     **/
    private static final String[] REQUIRED_RASTER_REQUEST_PARAMS = {"ullat", "ullon", "lrlat",
            "lrlon", "w", "h"};

    /**
     * The result of rastering must be a map containing all of the
     * fields listed in the comments for RasterAPIHandler.processRequest.
     **/
    private static final String[] REQUIRED_RASTER_RESULT_PARAMS = {"render_grid", "raster_ul_lon",
            "raster_ul_lat", "raster_lr_lon", "raster_lr_lat", "depth", "query_success"};


    @Override
    protected Map<String, Double> parseRequestParams(Request request) {
        return getRequestParams(request, REQUIRED_RASTER_REQUEST_PARAMS);
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     *
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     *
     * @param requestParams Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @param response : Not used by this function. You may ignore.
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image;
     *                    can also be interpreted as the length of the numbers in the image
     *                    string. <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>
     */
    @Override
    public Map<String, Object> processRequest(Map<String, Double> requestParams, Response response) {
        Map<String, Object> results = new HashMap<>();
        double S_L = 288200;
        double X = 0.087890625;
        double Y = 0.06939311371;
        double ulx = -122.2998046875;
        double lrx = -122.2119140625;
        double uly = 37.892195547244356;
        double lry = 37.82280243352756;
        double xDist = Math.abs(requestParams.get("lrlon") - requestParams.get("ullon"));
        double yDist = requestParams.get("ullat") - requestParams.get("lrlat");
        double width = requestParams.get("w");
        double height = requestParams.get("h");
        double desiredLonDPP = xDist * S_L / width;
        double inTheLog = S_L * X / desiredLonDPP / 256 ;
        double inTheCeiling = Math.log10(inTheLog) / Math.log10(2);
        double D = (int) Math.ceil(inTheCeiling);
        /** Corner Case #2*/
        if (requestParams.get("lrlon") < requestParams.get("ullon") || requestParams.get("ullat")
                < requestParams.get("lrlat")) {
            return this.queryFail();
        }
        if (D > 7) {
            D = 7;
        }
        int xStart = (int) Math.floor((requestParams.get("ullon") + 122.2998046875) / (X / Math.pow(2, D)));
        int xEnd = (int) Math.floor((requestParams.get("lrlon") + 122.2998046875) / (X / Math.pow(2, D)));
        int numberOfXTiles = (int) xEnd - xStart + 1;
        int tempYEnd = (int) Math.ceil((requestParams.get("ullat") - 37.82280243352756) / (Y / Math.pow(2, D)));
        int tempYStart = (int) Math.ceil((requestParams.get("lrlat") - 37.82280243352756) / (Y / Math.pow(2,D)));
        int numberOfYTiles = (int) tempYEnd - tempYStart + 1;
        int yStart = (int) Math.pow(2, D) - tempYEnd;
        int yEnd = (int) yStart + numberOfYTiles;
        results.put("raster_ul_lon", -122.2998046875 + (X / Math.pow(2, D)) * xStart);
        results.put("raster_ul_lat", 37.892195547244356 - (Y / Math.pow(2, D) * yStart));
        results.put("raster_lr_lon", -122.2998046875 + (X / Math.pow(2, D)) * (xEnd + 1));
        results.put("raster_lr_lat", 37.892195547244356 - (Y / Math.pow(2, D)) * yEnd);
        String[][] images = new String[numberOfYTiles][numberOfXTiles];
        int D_alpha = (int) D;
        results.put("depth", D_alpha);
        String row1 = "d" + D_alpha + "_" + "x" + xStart + "_" + "y" + yStart +".png";
        for (int j = 0; j < numberOfYTiles; j++) {
            for (int i = 0; i < numberOfXTiles; i++) {
                images[j][i] = "d" + D_alpha + "_x" + (xStart + i) + "_y" + (yStart + j) + ".png";
            }
        }
        results.put("render_grid", images);


        /** Need to determine the depth first*/
        /** Requirement for the depth d:
         * LonDPP = S_L * xDist / (256 * 2^(D - 1))
         * LatDPP = S_L * (yDist / (256 * 2^(D - 1))
         * minimumDPP = S_L * xDist / (256 * 2^(D-1))
         * 2^(D-1) = S_L * xDist / 256 / minimumDPP
         * D-1 = log_2(S_L * xDist / 256 / minimumDPP);
         * log_b(x) = log_k(x) / log_k(b);
         * log_2(x) = log_10(x) / log_10(b):
         * D = Math.ceil(log(S_L * xDist / minimumDPP / 256) + 1;
         * if (D > 7) {
         * D = 7;
         * }
         */

        /** Then decide the number of tiles
         * distance(X) for each tile = X / D;
         * distance(Y) for each tile = Y / D;
         * numOfTiles(X) = Math.ceil(width / DFET(X));
         * numbOfTiles(Y) = Math.ceil(height / DFET(Y));
         */
        /** Decide the starting point for X, Y
         * startX = Math.floor(requestParams.get(ullon) / (X / D));
         * startY = Math.floor(requestParams.get(ullat) / (Y / D));
         */
        /** Decide the ending point for X, Y
         * endX = Math.floor(requestParams.get(lrlon) / (X / D));
         * endY = Math.floor(reqeustParams.get(lrlat) / (Y / D));
         */
        results.put("query_success", true);
        return results;
    }

    @Override
    protected Object buildJsonResponse(Map<String, Object> result) {
        boolean rasterSuccess = validateRasteredImgParams(result);

        if (rasterSuccess) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            writeImagesToOutputStream(result, os);
            String encodedImage = Base64.getEncoder().encodeToString(os.toByteArray());
            result.put("b64_encoded_image_data", encodedImage);
        }
        return super.buildJsonResponse(result);
    }

    private Map<String, Object> queryFail() {
        Map<String, Object> results = new HashMap<>();
        results.put("render_grid", null);
        results.put("raster_ul_lon", 0);
        results.put("raster_ul_lat", 0);
        results.put("raster_lr_lon", 0);
        results.put("raster_lr_lat", 0);
        results.put("depth", 0);
        results.put("query_success", false);
        return results;
    }

    /**
     * Validates that Rasterer has returned a result that can be rendered.
     * @param rip : Parameters provided by the rasterer
     */
    private boolean validateRasteredImgParams(Map<String, Object> rip) {
        for (String p : REQUIRED_RASTER_RESULT_PARAMS) {
            if (!rip.containsKey(p)) {
                System.out.println("Your rastering result is missing the " + p + " field.");
                return false;
            }
        }
        if (rip.containsKey("query_success")) {
            boolean success = (boolean) rip.get("query_success");
            if (!success) {
                System.out.println("query_success was reported as a failure");
                return false;
            }
        }
        return true;
    }

    /**
     * Writes the images corresponding to rasteredImgParams to the output stream.
     * In Spring 2016, students had to do this on their own, but in 2017,
     * we made this into provided code since it was just a bit too low level.
     */
    private  void writeImagesToOutputStream(Map<String, Object> rasteredImageParams,
                                                  ByteArrayOutputStream os) {
        String[][] renderGrid = (String[][]) rasteredImageParams.get("render_grid");
        int numVertTiles = renderGrid.length;
        int numHorizTiles = renderGrid[0].length;

        BufferedImage img = new BufferedImage(numHorizTiles * Constants.TILE_SIZE,
                numVertTiles * Constants.TILE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics graphic = img.getGraphics();
        int x = 0, y = 0;

        for (int r = 0; r < numVertTiles; r += 1) {
            for (int c = 0; c < numHorizTiles; c += 1) {
                graphic.drawImage(getImage(Constants.IMG_ROOT + renderGrid[r][c]), x, y, null);
                x += Constants.TILE_SIZE;
                if (x >= img.getWidth()) {
                    x = 0;
                    y += Constants.TILE_SIZE;
                }
            }
        }

        /* If there is a route, draw it. */
        double ullon = (double) rasteredImageParams.get("raster_ul_lon"); //tiles.get(0).ulp;
        double ullat = (double) rasteredImageParams.get("raster_ul_lat"); //tiles.get(0).ulp;
        double lrlon = (double) rasteredImageParams.get("raster_lr_lon"); //tiles.get(0).ulp;
        double lrlat = (double) rasteredImageParams.get("raster_lr_lat"); //tiles.get(0).ulp;

        final double wdpp = (lrlon - ullon) / img.getWidth();
        final double hdpp = (ullat - lrlat) / img.getHeight();
        AugmentedStreetMapGraph graph = SEMANTIC_STREET_GRAPH;
        List<Long> route = ROUTE_LIST;

        if (route != null && !route.isEmpty()) {
            Graphics2D g2d = (Graphics2D) graphic;
            g2d.setColor(Constants.ROUTE_STROKE_COLOR);
            g2d.setStroke(new BasicStroke(Constants.ROUTE_STROKE_WIDTH_PX,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            route.stream().reduce((v, w) -> {
                g2d.drawLine((int) ((graph.lon(v) - ullon) * (1 / wdpp)),
                        (int) ((ullat - graph.lat(v)) * (1 / hdpp)),
                        (int) ((graph.lon(w) - ullon) * (1 / wdpp)),
                        (int) ((ullat - graph.lat(w)) * (1 / hdpp)));
                return w;
            });
        }

        rasteredImageParams.put("raster_width", img.getWidth());
        rasteredImageParams.put("raster_height", img.getHeight());

        try {
            ImageIO.write(img, "png", os);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private BufferedImage getImage(String imgPath) {
        BufferedImage tileImg = null;
        if (tileImg == null) {
            try {
                File in = new File(imgPath);
                tileImg = ImageIO.read(in);
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return tileImg;
    }
}
