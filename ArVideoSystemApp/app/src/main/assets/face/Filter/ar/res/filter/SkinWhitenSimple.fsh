precision highp float;
uniform sampler2D inputImageTexture;
varying vec2 textureCoordinate;
uniform float skinWhitenScaleValue;
const vec3 skinDefaultRGB = vec3(0.62, 0.47, 0.43);

 
 void main()
 {
     vec4 sourceTexture = texture2D(inputImageTexture, textureCoordinate);
     gl_FragColor = sourceTexture;
     
     
     //relation
     vec3 relation = vec3(1.0) + min((sourceTexture.xyz - skinDefaultRGB),vec3(0.0));
     float relationValue = 1./(1. + exp( 10. - 14. * (relation.x+relation.y+relation.z)/3.0) );
     
     float logParameter = skinWhitenScaleValue*1.8*relationValue + 1.;
     if (abs(logParameter -1.) < 0.01)
     {
         return;
     }
     
     gl_FragColor.rgb = log(gl_FragColor.rgb * (logParameter - 1.) + vec3(1., 1., 1.)) / log(logParameter);
     
 }
