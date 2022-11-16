import { spawnSync } from "child_process";
import { defineConfig } from "vite";

// Detect whether Vite runs in development or production mode
function isDev() {
  return process.env.NODE_ENV !== "production";
}

// Utility to invoke a given sbt task and fetch its output
function printSbtTask(task) {
  const args = ["--error", "--batch", `print ${task}`];
  const options = {
    stdio: [
      "pipe", // StdIn
      "pipe", // StdOut
      "inherit", // StdErr
    ],
  };
  const result = process.platform === 'win32'
    ? spawnSync("sbt.bat", args.map(x => `"${x}"`), {shell: true, ...options})
    : spawnSync("sbt", args, options);

  if (result.error)
    throw result.error;
  if (result.status !== 0)
    throw new Error(`sbt process failed with exit code ${result.status}`);
  return result.stdout.toString('utf8').trim();
}

// Get the output of fastLinkJS or fullLinkJS depending on isDev()
const scalaJSOutputTask = isDev() ? "fastLinkJSOutput" : "fullLinkJSOutput";
const scalaJSOutput = printSbtTask(scalaJSOutputTask);

// Tell Vite to replace references to `@scalaJSOutput` by the path computed above
export default defineConfig({
  resolve: {
    alias: [
      {
        find: "@scalaJSOutput",
        replacement: scalaJSOutput,
      },
    ],
  },
});
