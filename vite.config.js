import { defineConfig } from "vite";
import scalaJSPlugin from "@scala-js/vite-plugin-scalajs";

// Tell Vite to replace references to `@scalaJSOutput` by the path computed above
export default defineConfig({
  plugins: [
    scalaJSPlugin({
      //sbtProjectID: "livechart",
    }),
  ],
});
