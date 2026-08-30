CREATE TABLE `inspection_files` (
	`id` int AUTO_INCREMENT NOT NULL,
	`inspectionId` varchar(64) NOT NULL,
	`storedFileId` int NOT NULL,
	`createdAt` timestamp NOT NULL DEFAULT (now()),
	CONSTRAINT `inspection_files_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
CREATE TABLE `inspections` (
	`id` varchar(64) NOT NULL,
	`ownerId` int NOT NULL,
	`productName` varchar(255) NOT NULL,
	`manufacturer` varchar(255) NOT NULL,
	`status` enum('COMPLIANT','NON-COMPLIANT','NEEDS REVIEW') NOT NULL,
	`declarations` text NOT NULL,
	`violations` text NOT NULL,
	`confidence` int NOT NULL,
	`location` varchar(255) NOT NULL,
	`createdAt` timestamp NOT NULL DEFAULT (now()),
	CONSTRAINT `inspections_id` PRIMARY KEY(`id`)
);
