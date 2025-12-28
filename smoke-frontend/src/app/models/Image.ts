export interface Image {
  imageId: number;
  image?: string; // base64 encoded image
  gameId?: number;
  url?: string; // for backward compatibility
  description?: string;
  videogameId?: number;
}