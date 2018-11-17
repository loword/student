precision highp float;
uniform sampler2D inputImageTexture;
varying vec2 textureCoordinate;
uniform float skinSmoothScaleValue;
const vec3 skinDefaultRGB = vec3(0.62, 0.47, 0.43);
const int winsize = 6;
const float minstep = 4.0;
const float texelWidthOffset = 1./720.;
const float texelHeightOffset = 1./1280.;

const float xInc = minstep * texelWidthOffset;
const float yInc = minstep * texelHeightOffset;
const float minEps = 0.00001;

void main()
{
    vec4 sourceTexture = texture2D(inputImageTexture, textureCoordinate);
    
    //relation
    vec3 relation = vec3(1.0) + min((sourceTexture.xyz - skinDefaultRGB),vec3(0.0));
    float relationValue = 1./(1. + exp( 24. - 30. * (relation.x+relation.y+relation.z)/3.0) );
//    relationValue = 1.0;
    //eps
    float eps = 0.0015 * skinSmoothScaleValue * skinSmoothScaleValue * relationValue;
    if (eps < minEps) {
        gl_FragColor = sourceTexture;//vec4(1.0, 0.0, 0.0, 1.0);
        return;
    }
    
    //meanI meanII
    vec3 meanI = vec3(0., 0., 0.);
    vec3 meanII = vec3(0., 0., 0.);
    for(int i = 0; i < winsize; i++) {
          for(int j = 0; j < winsize; j++) {
              vec3 aroundPixel = texture2D(inputImageTexture, vec2( textureCoordinate.x + (-float(winsize / 2) * xInc) + float(i) * xInc, textureCoordinate.y + (-float(winsize / 2) * yInc) + float(j) * yInc)).rgb;
              meanI += aroundPixel;
              meanII += aroundPixel * aroundPixel;
          }
    }
    meanI /= float(winsize * winsize);
    meanII /= float(winsize * winsize);
    
    //variance
    vec3 variance = meanII - meanI * meanI;
    vec3 a = variance / (variance + eps);
    vec3 b = meanI - a * meanI;
    gl_FragColor.rgb =  a * sourceTexture.rgb + b;
    
//     gl_FragColor.rgb = vec3(relationValue);
     gl_FragColor.a = 1.0;


 }
