import { spawn } from "child_process";

// Utility to invoke a given sbt task and fetch its output
function printSbtTask(task) {
  const args = ["--batch", `print ${task}`];
  const options = {};
  const child = process.platform === 'win32'
    ? spawn("sbt.bat", args.map(x => `"${x}"`), {shell: true, ...options})
    : spawn("sbt", args, options);

  let fullOutput = '';

  child.stdout.setEncoding('utf-8');
  child.stdout.on('data', data => {
    fullOutput += data;
    process.stdout.write(data); // tee on my own stdout
  });

  child.stderr.setEncoding('utf-8');
  child.stderr.on('data', data => {
    process.stderr.write(data); // tee on my own stderr
  });

  return new Promise((resolve, reject) => {
    child.on('close', code => {
      if (code !== 0)
        reject(new Error(`sbt invocation for Scala.js compilation failed with exit code ${code}`));
      else
        resolve(fullOutput.trimEnd().split('\n').at(-1));
    })
  })
}

export default function scalaJSPlugin(options = {}) {
  const { sbtProjectID } = options;

  let isDev = false; // when resolving the Vite config, this may turn into true
  let scalaJSOutputDir;

  return {
    name: "scalajs:sbt-scalajs-plugin",

    // Vite-specific
    configResolved(resolvedConfig) {
      isDev = resolvedConfig.mode === 'development';
    },

    // standard Rollup
    async buildStart(options) {
      const task = isDev ? "fastLinkJSOutput" : "fullLinkJSOutput";
      const projectTask = sbtProjectID ? `${sbtProjectID}/${task}` : task;
      scalaJSOutputDir = await printSbtTask(projectTask);
    },

    // standard Rollup
    resolveId(source, importer, options) {
      const parts = /^scalajs:(.*)$/g.exec(source);
      if (!parts)
        return null;

      return `${scalaJSOutputDir}/${parts[1]}`;
    },
  };
}
