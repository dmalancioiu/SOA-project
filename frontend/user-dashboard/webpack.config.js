const HtmlWebpackPlugin = require('html-webpack-plugin');
// FIX: Use the standard built-in plugin from 'webpack'
const { ModuleFederationPlugin } = require('webpack').container;
const Dotenv = require('dotenv-webpack');
const path = require('path');

module.exports = {
  entry: './src/index.js',
  mode: process.env.NODE_ENV || 'development',
  devServer: {
    port: 3003,
    historyApiFallback: true,
    hot: true,
    headers: {
      'Access-Control-Allow-Origin': '*',
    },
  },
  output: {
    publicPath: 'auto',
    clean: true,
  },
  resolve: {
    extensions: ['.js', '.jsx'],
    fallback: {
      "net": false,
      "tls": false,
      "fs": false
    }
  },
  module: {
    rules: [
      {
        test: /\.jsx?$/,
        exclude: /node_modules/,
        use: {
          loader: 'babel-loader',
          options: {
            presets: ['@babel/preset-env', '@babel/preset-react'],
          },
        },
      },
      // Add CSS loader support just in case
      {
        test: /\.css$/i,
        use: ["style-loader", "css-loader"],
      },
    ],
  },
  plugins: [
    new ModuleFederationPlugin({
      name: 'userDashboard',
      filename: 'remoteEntry.js',
      exposes: {
        './UserDashboard': './src/UserDashboard',
      },
      shared: {
        react: { singleton: true, requiredVersion: '^18.2.0' },
        'react-dom': { singleton: true, requiredVersion: '^18.2.0' },
        'react-router-dom': { singleton: true, requiredVersion: '^6.20.1' },
        '@mui/material': { singleton: true },
        '@emotion/react': { singleton: true },
        '@emotion/styled': { singleton: true },
        // Add these to shared to prevent duplicate socket connections
        'sockjs-client': { singleton: true },
        '@stomp/stompjs': { singleton: true }
      },
    }),
    new HtmlWebpackPlugin({
      template: './public/index.html',
    }),
    new Dotenv(),
  ],
};