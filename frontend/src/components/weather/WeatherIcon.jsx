import {
  Sun,
  CloudSun,
  Cloud,
  CloudDrizzle,
  CloudRain,
  CloudLightning,
  CloudFog,
} from 'lucide-react';

// Weather condition codes shared by the sample data and the expected backend response.
export const CONDITIONS = {
  sunny: { label: 'Sunny', Icon: Sun, color: '#e09a1f' },
  partly_cloudy: { label: 'Partly cloudy', Icon: CloudSun, color: '#e09a1f' },
  cloudy: { label: 'Cloudy', Icon: Cloud, color: '#6f8a7a' },
  light_rain: { label: 'Light rain', Icon: CloudDrizzle, color: '#3f82c4' },
  heavy_rain: { label: 'Heavy rain', Icon: CloudRain, color: '#2b6cb0' },
  thunderstorm: { label: 'Thunderstorm', Icon: CloudLightning, color: '#6b46c1' },
  fog: { label: 'Fog / mist', Icon: CloudFog, color: '#8a9a90' },
};

// Unknown codes from a future backend fall back to "cloudy" instead of crashing the page.
export const conditionMeta = (code) => CONDITIONS[code] || CONDITIONS.cloudy;

export default function WeatherIcon({ condition, size = 28 }) {
  const { Icon, color, label } = conditionMeta(condition);
  return <Icon size={size} color={color} aria-label={label} role="img" />;
}
