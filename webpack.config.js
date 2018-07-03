const path = require('path');
const webpack = require('webpack');
const MiniCssExtractPlugin = require("mini-css-extract-plugin");

module.exports = {
  entry: './src/main/js/index.js',
  output: {
    filename: 'calendar-view.js',
    path: path.join(__dirname, 'src/main/webapp/')
  },
  devtool: 'sourcemap',
  resolve: {
    extensions: [ '.js' ],
  },
  module: {
    rules: [
      {
        test: /\.css$/,
        use: [
          {
            loader: MiniCssExtractPlugin.loader,
          },
          'css-loader'
        ]
      }
    ]
  },
  plugins: [
    new webpack.IgnorePlugin(/^\.\/locale$/, /moment$/),
    new MiniCssExtractPlugin({
      filename: 'calendar-view.css',
      chunkFilename: '[id].css'
    })
  ]
};
