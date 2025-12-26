export interface Videogame {
  videogameId: number;
  relasedate: string; // Date as string
  minimRequirements: string;
  price: number;
  description: string;
  name: string;
  companyId: number;
  ageRestriction: number;
  available: boolean;
}