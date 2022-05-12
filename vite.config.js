import { spawnSync } from "child_process";
import { defineConfig } from "vite";

function isDev() {
  return process.env.NODE_ENV !== "production";
}

function printSbtTask(task) {
  const result = spawnSync("sbt", ["--error", "--batch", `print ${task}`], {
    stdio: [
      "pipe", // StdIn.
      "pipe", // StdOut.
      "inherit", // StdErr.
    ],
  });

  return result.stdout.toString('utf8').trim();
}

const replacementForPublic = isDev()
  ? printSbtTask("publicDev")
  : printSbtTask("publicProd");

export default defineConfig({
  resolve: {
    alias: [
      {
        find: "@public",
        replacement: replacementForPublic,
      },
    ],
  },
});
