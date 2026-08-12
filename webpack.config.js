const path = require('path');

// Bit of a hack to avoid using two config files
module.exports = (env, argv) => {
    const isProduction = argv.mode === 'production';

    return {
        mode: isProduction ? 'production' : 'development',
        entry: './src/main/ts/editor.ts',
        devtool: isProduction ? 'source-map' : 'eval-source-map',
        module: {
            rules: [
                {
                    test: /\.ts$/,
                    use: 'ts-loader',
                    exclude: /node_modules/
                }
            ]
        },
        resolve: {
            extensions: ['.ts', '.js'],
            alias: {
                '@': path.resolve(__dirname, 'src/main/ts'),
            }
        },
        output: {
            filename: 'editor.js',
            path: path.resolve(__dirname,
                    'src/main/resources/static/js/editor/'),
            clean: true
        }
    };
};