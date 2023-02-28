import { defineConfig } from "vite";
import scalaJSPlugin from "./vite-plugin-sbt-scalajs.mjs";

// Tell Vite to replace references to `@scalaJSOutput` by the path computed above
export default defineConfig({
  plugins: [
    scalaJSPlugin({
      //sbtProjectID: "livechart",
    }),
  ],
});
