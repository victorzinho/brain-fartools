// noinspection NodeCoreCodingAssistance
const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
  entry: {
    index: './src/main.js'
  },
  resolve: {
    fallback: {
      fs: false
    }
  },
  plugins: [
    new HtmlWebpackPlugin({
      template: 'index.html'
    })
  ],
  output: {
    path: path.resolve(__dirname, 'dist'),
    filename: 'bundle.js'
  },
  module: {
    rules: [
      {
        test: /\.(png|svg|jpg|jpeg|gif)$/i,
        type: 'asset/resource'
      }, {
        test: /\.css$/i,
        use: [
          'style-loader',
          'css-loader'
        ]
      }
    ]
  }
};
