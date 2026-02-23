@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")  // ← これに変更
                .allowedMethods("*")
                .allowedHeaders("*");
    }
}