/**
 * Provides access to the design details loaded into the template with validation.
 */
export class DesignDataProvider {
    private static renovationID: number | null = null;
    private static designID: number | null = null;
    private static owned: boolean | null = null;
    private static renovationDesign: boolean | null = null;

    public static getRenovationID(): number {
        if (this.renovationID === null) {
            const renovationIDInput = document.getElementById("renovationId") as HTMLInputElement;

            if (!renovationIDInput) {
                throw new Error("renovation id input element is missing");
            }

            this.renovationID = parseInt(renovationIDInput.value);
        }
        return this.renovationID;
    }

    public static getDesignID(): number {
        if (this.designID === null) {
            const designIDInput = document.getElementById("designId") as HTMLInputElement;

            if (!designIDInput) {
                throw new Error("design id input element is missing");
            }

            this.designID = parseInt(designIDInput.value);

        }
        return this.designID;
    }

    public static isOwned(): boolean {
        if (this.owned === null) {
            const ownedInput = document.getElementById("owned") as HTMLInputElement;

            if (!ownedInput) {
                throw new Error("owned input element is missing");
            }

            switch (ownedInput.value.trim().toLowerCase()) {
                case 'true':
                    this.owned = true;
                    break;

                case 'false':
                    this.owned = false;
                    break;

                default:
                    throw new Error("unable to parse boolean from owned input element");
            }
        }

        return this.owned;
    }

    public static isRenovationDesign(): boolean {
        if (this.renovationDesign === null) {
            const isRenovationDesignInput = document.getElementById("isRenovationDesign") as HTMLInputElement;

            if (!isRenovationDesignInput) {
                throw new Error("owned input element is missing");
            }

            switch (isRenovationDesignInput.value.trim().toLowerCase()) {
                case 'true':
                    this.renovationDesign = true;
                    break;

                case 'false':
                    this.renovationDesign = false;
                    break;

                default:
                    throw new Error("unable to parse boolean from owned input element");
            }
        }

        return this.renovationDesign;
    }
}