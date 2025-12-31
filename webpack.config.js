const path = require('path');
const webpack = require('webpack');

const CssMinimizerPlugin = require('css-minimizer-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const TerserPlugin = require('terser-webpack-plugin');

module.exports = {
  entry: './src/main/js/index.js',
  output: {
    filename: 'calendar-view.js',
    path: path.join(__dirname, 'src/main/webapp/bundles')
  },
  externals: {
    jquery: 'jQuery'
  },
  devtool: 'source-map',
  resolve: {
    extensions: [ '.js' ]
  },
  optimization: {
    minimizer: [
      new TerserPlugin({}),
      new CssMinimizerPlugin({})
    ]
  },
  module: {
    rules: [
      {
        test: /\.css$/,
        use: [
          {
            loader: MiniCssExtractPlugin.loader
          },
          'css-loader'
        ]
      },
      {
        test: /\.js$/,
        exclude: /node_modules/,
        loader: 'babel-loader'
      },
      {
        test: /\.js$/,
        exclude: /node_modules/,
        loader: 'eslint-loader'
      }
    ]
  },
  plugins: [
    new webpack.IgnorePlugin({resourceRegExp: /^\.\/locale$/, contextRegExp: /moment$/}),
    new MiniCssExtractPlugin({
      filename: 'calendar-view.css',
      chunkFilename: '[id].css'
    })
  ]
};
