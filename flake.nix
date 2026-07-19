{
  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixpkgs-unstable";
    flake-parts = {
      url = "github:hercules-ci/flake-parts";
      inputs.nixpkgs-lib.follows = "nixpkgs";
    };
    pkgs-by-name.url = "github:drupol/pkgs-by-name-for-flake-parts";

    baritone = {
      url = "github:TheColorman/baritone/1.21.11";
      inputs = {
        nixpkgs.follows = "nixpkgs";
        flake-parts.follows = "flake-parts";
        pkgs-by-name.follows = "pkgs-by-name";
      };
    };
  };

  outputs =
    inputs:
    inputs.flake-parts.lib.mkFlake { inherit inputs; } (
      { lib, ... }: {
        imports = [ inputs.pkgs-by-name.flakeModule ];

        systems = lib.systems.flakeExposed;

        perSystem =
          {
            pkgs,
            system,
            self',
            ...
          }:
          {
            pkgsDirectory = ./nix/pkgs;
            packages = {
              baritone = inputs.baritone.packages.${system}.default;
              default = self'.packages.meteor;
            };

            devShells.default = pkgs.mkShellNoCC {
              packages = with pkgs; [
                gradle_9
                jdk25
              ];
            };
          };
      }
    );
}
